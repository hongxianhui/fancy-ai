package com.fancy.aichat.client.handler;

import com.fancy.aichat.client.QuestionHandler;
import com.fancy.aichat.client.tts.ChatStreamTokenizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.fancy.aichat.common.Answer;
import org.fancy.aichat.common.Question;
import org.fancy.aichat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;

public abstract class AbstractQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract Prompt generatePrompt(Question question);

    protected abstract ChatClient getChatClient();

    @Override
    public void handle(Question question, PrintWriter writer) throws IOException {
        long time = System.currentTimeMillis();
        Prompt prompt = generatePrompt(question);
        getChatClient().prompt(prompt).stream().chatResponse().subscribe(new Consumer<>() {
            boolean think = false;
            boolean started = false;
            final ChatStreamTokenizer tokenizer = new ChatStreamTokenizer();

            @Override
            public void accept(ChatResponse chatResponse) {
                String token = chatResponse.getResult().getOutput().getText();
                boolean done = Boolean.TRUE.equals(chatResponse.getMetadata().get("done"));
                if (token.contains("<think>")) {
                    think = true;
                    logger.info("Get answer stream cost {},", System.currentTimeMillis() - time);
                    return;
                }
                if (token.contains("</think>")) {
                    think = false;
                    return;
                }
                if (!started && "\n".equals(token)) {
                    return;
                }
                if ("\n\n".equals(token)) {
                    token = "\n";
                }
                started = true;
                Answer.Builder builder = Answer.builder().user(question.getUser()).type(think ? Answer.TYPE_THINK : Answer.TYPE_ANSWER).content(token);
                if (done) {
                    builder.done();
                    logger.info("Answer total cost {},", System.currentTimeMillis() - time);
                }
                String sentence = tokenizer.tokenize(token);
                try {
                    if (!sentence.isBlank()) {
                        writer.println(Utils.serialize(Answer.builder().user(question.getUser()).type(Answer.TYPE_SPEECH).content(sentence).build()));
                        writer.flush();
                    }
                    writer.println(Utils.serialize(builder.build()));
                    writer.flush();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    //ignore
                }
            }
        });
    }
}
