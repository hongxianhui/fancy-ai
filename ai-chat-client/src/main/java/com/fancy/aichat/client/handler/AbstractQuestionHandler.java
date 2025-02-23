package com.fancy.aichat.client.handler;

import com.fancy.aichat.client.QuestionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.fancy.aichat.common.Answer;
import org.fancy.aichat.common.Question;
import org.fancy.aichat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class AbstractQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ChatModel chatModel;

    protected abstract ChatModel createChatModel();

    protected abstract Prompt generatePrompt(Question question);

    private ChatModel getChatModel() {
        if (chatModel == null) {
            chatModel = createChatModel();
        }
        return chatModel;
    }

    @Override
    public void handle(Question question, PrintWriter writer) throws IOException {
        final boolean[] thinking = new boolean[1];
        long time = System.currentTimeMillis();
        ChatModel chatModel = getChatModel();
        Prompt prompt = generatePrompt(question);
        chatModel.stream(prompt).subscribe(chatResponse -> {
            String text = chatResponse.getResult().getOutput().getText();
            if (text.contains("<think>")) {
                thinking[0] = true;
                logger.info("Get answer stream cost {},", System.currentTimeMillis() - time);
                return;
            }
            if (text.contains("</think>")) {
                thinking[0] = false;
                return;
            }
            Answer.Builder builder = Answer.builder().user(question.getUser()).type(thinking[0] ? Answer.TYPE_THINK : Answer.TYPE_ANSWER).content(text);
            if (Boolean.TRUE.equals(chatResponse.getMetadata().get("done"))) {
                builder.done();
                logger.info("Answer complete total cost {},", System.currentTimeMillis() - time);
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
