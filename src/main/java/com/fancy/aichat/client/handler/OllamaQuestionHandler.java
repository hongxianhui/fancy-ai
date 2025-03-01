package com.fancy.aichat.client.handler;

import com.fancy.aichat.client.QuestionHandler;
import com.fancy.aichat.client.StreamTokenizer;
import com.fancy.aichat.client.tts.ChatStreamTokenizer;
import com.fancy.aichat.client.tools.AdministrationTool;
import com.fancy.aichat.client.tools.ChatTool;
import com.fancy.aichat.client.tools.KnowledgeTool;
import com.fancy.aichat.objects.Answer;
import com.fancy.aichat.objects.Question;
import com.fancy.aichat.objects.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
@Order(20)
public class OllamaQuestionHandler implements QuestionHandler, InitializingBean {
    public static final String MODEL_NAME = "qwen2.5:0.5b";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ChatClient chatClient;

    @Value("${ai.master.password}")
    private String masterPassword;

    @Override
    public void afterPropertiesSet() {
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        toolCallbacks.addAll(ChatTool.generateToolCallbacks(AdministrationTool.class));
        toolCallbacks.addAll(ChatTool.generateToolCallbacks(KnowledgeTool.class));
        OllamaOptions chatOptions = OllamaOptions.builder().build();
        OllamaChatModel chatModel = OllamaChatModel.builder().ollamaApi(new OllamaApi()).defaultOptions(chatOptions).build();
        chatClient = ChatClient.builder(chatModel)
                .defaultSystem(ResourceUtils.getText("classpath:prompt/local-identity.txt"))
                .defaultTools(toolCallbacks)
                .build();
    }

    @Override
    public boolean handle(Question question) throws IOException {
        if (!MODEL_NAME.equals(question.getUser().getModel())) {
            return false;
        }
        logger.info("Handler: {}, Question: {}", getClass().getName(), Utils.serialize(question));
        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put("masterPassword", masterPassword);
        toolContext.put("question", question);
        OllamaOptions ollamaOptions = OllamaOptions.builder()
                .model(MODEL_NAME)
                .toolContext(toolContext)
                .build();
        Prompt prompt = new Prompt(question.getContent(), ollamaOptions);
        chatClient.prompt(prompt).stream().chatResponse().subscribe(new Consumer<>() {
            final StreamTokenizer tokenizer = new ChatStreamTokenizer(10);
            final WebSocketSession out = question.getUser().getSession();

            @Override
            public void accept(ChatResponse chatResponse) {
                try {
                    String content = chatResponse.getResult().getOutput().getText();
                    Answer.AnswerBuilder builder = Answer.builder()
                            .user(question.getUser())
                            .content(content);
                    String toolName = chatResponse.getResult().getMetadata().get("toolName");
                    if (StringUtils.hasText(toolName)) {
                        builder.type(Answer.TYPE_TOOL);
                    }
                    out.sendMessage(new TextMessage(Utils.serialize(builder.build())));
                } catch (Exception e) {
                    logger.error("ollama cll error", e);
                }
            }
        });
        return true;
    }
}
