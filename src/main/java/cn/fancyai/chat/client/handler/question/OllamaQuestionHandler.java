package cn.fancyai.chat.client.handler.question;

import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.client.handler.answer.AnswerCallback;
import cn.fancyai.chat.client.tools.AdministrationTool;
import cn.fancyai.chat.client.tools.ChatTool;
import cn.fancyai.chat.client.tools.KnowledgeTool;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.objects.Question;
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
    public boolean handle(Question question, AnswerCallback callback) throws IOException {
        if (!MODEL_NAME.equals(question.getUser().getModel())) {
            return false;
        }
        logger.info("Handler: {}, Question: {}", getClass().getName(), ChatUtils.serialize(question));
        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put("question", question);
        OllamaOptions ollamaOptions = OllamaOptions.builder()
                .model(MODEL_NAME)
                .toolContext(toolContext)
                .build();
        Prompt prompt = new Prompt(question.getContent(), ollamaOptions);
        chatClient.prompt(prompt).stream().chatResponse().subscribe(chatResponse -> {
            try {
                String content = chatResponse.getResult().getOutput().getText();
                Answer.AnswerBuilder builder = Answer.builder()
                        .user(question.getUser())
                        .content(content);
                String toolName = chatResponse.getResult().getMetadata().get("toolName");
                if (StringUtils.hasText(toolName)) {
                    builder.type(Answer.TYPE_TOOL);
                }
                callback.onAnswer(builder.build());
            } catch (Exception e) {
                logger.error("ollama cll error", e);
            }
        });
        return true;
    }
}
