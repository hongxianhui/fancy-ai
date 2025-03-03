package cn.fancyai.chat.client.handler.text;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.client.tools.AdministrationTool;
import cn.fancyai.chat.client.tools.ChatTool;
import cn.fancyai.chat.client.tools.KnowledgeTool;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(520)
public class OllamaQuestionHandler implements QuestionHandler, InitializingBean {
    public static final String MODEL_NAME = "qwen2.5:0.5b";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserManager userManager;

    private ChatClient chatClient;

    public OllamaQuestionHandler(UserManager userManager) {
        this.userManager = userManager;
    }

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
    public boolean handle(Question question, HandlerContext context) throws IOException {
        User user = question.getUser();
        if (!MODEL_NAME.equals(user.getModel().getChat())) {
            return false;
        }
        logger.info("Handler: {}, Question: {}", getClass().getSimpleName(), ChatUtils.serialize(question));
        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put("question", question);
        toolContext.put("handlerContext", context);
        OllamaOptions ollamaOptions = OllamaOptions.builder()
                .model(MODEL_NAME)
                .toolContext(toolContext)
                .build();
        Prompt prompt = new Prompt(question.getContent(), ollamaOptions);
        chatClient.prompt(prompt).stream().chatResponse().subscribe(chatResponse -> {
            try {
                String content = chatResponse.getResult().getOutput().getText();
                String toolName = chatResponse.getResult().getMetadata().get("toolName");
                Boolean done = chatResponse.getMetadata().get("done");
                Answer answer = Answer.builder()
                        .user(user)
                        .content(content)
                        .build();
                if (Boolean.TRUE.equals(done)) {
                    answer.setDone(true);
                }
                if (StringUtils.hasText(toolName)) {
                    answer = ChatUtils.deserialize(content, Answer.class);
                    answer.setUser(userManager.getUser(answer.getUser().getUserId()));
                }
                user.getChatSession().sendMessage(answer, context);
                if (Boolean.TRUE.equals(done)) {
                    logger.info("Answer complete.");
                }
            } catch (Exception e) {
                logger.error("ollama call error", e);
            }
        });
        return true;
    }
}
