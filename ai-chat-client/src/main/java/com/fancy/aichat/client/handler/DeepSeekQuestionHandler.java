package com.fancy.aichat.client.handler;

import org.fancy.aichat.common.Answer;
import org.fancy.aichat.common.ChatPrompt;
import org.fancy.aichat.common.Question;
import org.fancy.aichat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@Order(2)
public class DeepSeekQuestionHandler extends AbstractQuestionHandler {
    private final String MODEL_NAME = "deepseek-r1:1.5b";

    protected ChatModel createChatModel() {
        return OllamaChatModel.builder().ollamaApi(new OllamaApi()).build();
    }

    @Override
    protected Prompt generatePrompt(Question question) {
        return new Prompt(question.getContent(), OllamaOptions.builder().model(MODEL_NAME).build());
    }

    @Override
    public void handle(Question question, PrintWriter writer) throws IOException {
        if (ChatPrompt.CALL_FUNCTION == question.getPrompt()) {
            String content = "deepseek大模型目前不支持函数调用。";
            writer.println(Utils.serialize(Answer.builder().user(question.getUser()).type(Answer.TYPE_ANSWER).content(content).build()));
            writer.flush();
            return;
        }
        super.handle(question, writer);
    }

    @Override
    public boolean support(Question question) {
        return MODEL_NAME.equals(question.getUser().getModel());
    }
}
