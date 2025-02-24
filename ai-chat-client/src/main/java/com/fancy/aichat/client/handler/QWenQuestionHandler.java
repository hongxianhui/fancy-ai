package com.fancy.aichat.client.handler;

import com.fancy.aichat.tools.Tools;
import jakarta.annotation.Resource;
import org.fancy.aichat.common.ChatPrompt;
import org.fancy.aichat.common.Question;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(3)
public class QWenQuestionHandler extends AbstractQuestionHandler {
    private final String MODEL_NAME = "qwen2.5:1.5b";

    @Resource
    private List<Tools> tools;
    @Resource
    private VectorStore vectorStore;

    @Override
    protected ChatClient createChatClient() {
        OllamaOptions chatOptions = OllamaOptions.builder().toolCallbacks(ToolCallbacks.from(tools.toArray())).build();
        OllamaChatModel chatModel = OllamaChatModel.builder().ollamaApi(new OllamaApi()).defaultOptions(chatOptions).build();
        return ChatClient.builder(chatModel).defaultSystem(ResourceUtils.getText("classpath:qwen-default-system.txt")).defaultTools(tools).build();
    }

    @Override
    protected Prompt generatePrompt(Question question) {
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(question.getContent()));
        if (ChatPrompt.CALL_FUNCTION.equals(question.getPrompt())) {
            messages.add(0, new SystemMessage(ResourceUtils.getText("classpath:qwenprompt.txt")));
        }
        if (ChatPrompt.USE_KNOWLEDGE.equals(question.getPrompt())) {
            String content = question.getContent();
            List<Document> documents = vectorStore.similaritySearch(content);
            if (CollectionUtils.isEmpty(documents)) {
                logger.info("Knowledge not found.");
            } else {
                StringBuffer knowledge = new StringBuffer();
                for (Document document : documents) {
                    knowledge.append(document.getText());
                    knowledge.append("\n");
                }
                messages.add(0, new SystemMessage("严格根据以下信息回答用户问题：\n" + knowledge));
            }
        }
        return new Prompt(messages, OllamaOptions.builder().model(MODEL_NAME).build());
    }

    @Override
    public boolean support(Question question) {
        return MODEL_NAME.equals(question.getUser().getModel());
    }
}
