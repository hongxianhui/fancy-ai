package com.fancy.aichat.client.handler;

import org.fancy.aichat.common.Question;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class DeepSeekQuestionHandler extends AbstractQuestionHandler {
    private final String MODEL_NAME = "deepseek-r1:1.5b";

    private final ChatClient chatClient;

    protected DeepSeekQuestionHandler(VectorStore vectorStore, ChatMemory chatMemory) {
        this.chatClient = ChatClient.builder(OllamaChatModel.builder().ollamaApi(new OllamaApi()).build())
//                .defaultAdvisors(
//                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().build(), ResourceUtils.getText("classpath:ARG-default-context.txt")),
//                        new MessageChatMemoryAdvisor(chatMemory),
//                        new SimpleLoggerAdvisor()
//                )
                .build();
    }

    @Override
    protected ChatClient getChatClient() {
        return chatClient;
    }

    @Override
    protected Prompt generatePrompt(Question question) {
        return new Prompt(question.getContent(), OllamaOptions.builder().model(MODEL_NAME).build());
    }

    @Override
    public boolean support(Question question) {
        return MODEL_NAME.equals(question.getUser().getModel());
    }
}
