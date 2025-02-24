package com.fancy.aichat.client.handler;

import com.fancy.aichat.client.QuestionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.fancy.aichat.common.Answer;
import org.fancy.aichat.common.Question;
import org.fancy.aichat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class AbstractQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ChatClient chatClient;

    protected abstract ChatClient createChatClient();

    protected abstract Prompt generatePrompt(Question question);

    private ChatClient getChatClient() {
        if (chatClient == null) {
            chatClient = createChatClient();
        }
        return chatClient;
    }

    @Override
    public void handle(Question question, PrintWriter writer) throws IOException {
        final boolean[] thinking = new boolean[1];
        long time = System.currentTimeMillis();
        Prompt prompt = generatePrompt(question);
        StringBuffer fullAnswer = new StringBuffer();
        getChatClient().prompt(prompt).stream().chatResponse().subscribe(chatResponse -> {
            String token = chatResponse.getResult().getOutput().getText();
            boolean done = Boolean.TRUE.equals(chatResponse.getMetadata().get("done"));
            if (token.contains("<think>")) {
                thinking[0] = true;
                logger.info("Get answer stream cost {},", System.currentTimeMillis() - time);
                fullAnswer.append(token);
                return;
            }
            if (token.contains("</think>")) {
                thinking[0] = false;
                fullAnswer.append(token);
                return;
            }
            if (fullAnswer.isEmpty() && "\n".equals(token)) {
                return;
            }
            fullAnswer.append(token);
            Answer.Builder builder = Answer.builder().user(question.getUser()).type(thinking[0] ? Answer.TYPE_THINK : Answer.TYPE_ANSWER).content(token);
            if (done) {
                builder.done();
                logger.info("Answer total cost {},", System.currentTimeMillis() - time);
                logger.info("Answer: {}", fullAnswer);
            }
            try {
                writer.println(Utils.serialize(builder.build()));
                writer.flush();
            } catch (JsonProcessingException e) {
                //ignore
            }
        });
    }
}
