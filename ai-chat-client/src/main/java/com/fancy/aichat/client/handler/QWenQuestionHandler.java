package com.fancy.aichat.client.handler;

import com.fancy.aichat.tools.ChatTool;
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
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
public class QWenQuestionHandler extends AbstractQuestionHandler {
    private final String MODEL_NAME = "qwen2.5:1.5b";

    private final ChatClient chatClient;

    protected QWenQuestionHandler(VectorStore vectorStore, List<ChatTool> tools, ChatMemory chatMemory) {
        OllamaOptions chatOptions = OllamaOptions.builder().toolCallbacks(ToolCallbacks.from(tools.toArray())).build();
        this.chatClient = ChatClient.builder(OllamaChatModel.builder().ollamaApi(new OllamaApi()).defaultOptions(chatOptions).build())
                .defaultSystem("classpath:qwen-default-system.txt")
                .defaultAdvisors(
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().build(), ResourceUtils.getText("classpath:ARG-default-context.txt")),
                        new MessageChatMemoryAdvisor(chatMemory),
                        new SimpleLoggerAdvisor()
                )
                .defaultTools(tools)
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
