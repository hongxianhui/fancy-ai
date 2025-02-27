package com.fancy.aichat.client.handler;

import com.fancy.aichat.client.QuestionHandler;
import com.fancy.aichat.client.StreamTokenizer;
import com.fancy.aichat.client.tokenizer.ChatStreamTokenizer;
import com.fancy.aichat.client.tools.administration.ActiveMasterUserTool;
import com.fancy.aichat.client.tools.administration.QueryMasterTool;
import com.fancy.aichat.client.tools.administration.SwitchModelTool;
import com.fancy.aichat.common.Answer;
import com.fancy.aichat.common.Question;
import com.fancy.aichat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class OllamaQuestionHandler implements QuestionHandler, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String MODEL_NAME = "qwen2.5:0.5b";

    private ChatClient chatClient;

    @Value("${ai.master.password}")
    private String masterPassword;

    @Override
    public void afterPropertiesSet() {
        OllamaOptions chatOptions = OllamaOptions.builder().build();
        OllamaChatModel chatModel = OllamaChatModel.builder().ollamaApi(new OllamaApi()).defaultOptions(chatOptions).build();
        chatClient = ChatClient.builder(chatModel)
                .defaultSystem(ResourceUtils.getText("classpath:prompt/local-identity.txt"))
                .defaultTools(
                        new ActiveMasterUserTool(),
                        new QueryMasterTool(),
                        new SwitchModelTool())
                .build();
    }

    @Override
    public void handle(Question question) throws IOException {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(ResourceUtils.getText("classpath:prompt/local-tool.txt")));
        messages.add(new UserMessage(question.getContent()));
        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put("masterPassword", masterPassword);
        toolContext.put("user", question.getUser());
        OllamaOptions ollamaOptions = OllamaOptions.builder()
                .model(MODEL_NAME)
                .toolNames("activeMasterUser", "queryMaster", "switchModel")
                .toolContext(toolContext)
                .build();
        Prompt prompt = new Prompt(messages, ollamaOptions);
        chatClient.prompt(prompt).stream().chatResponse().subscribe(new Consumer<>() {
            final StreamTokenizer tokenizer = new ChatStreamTokenizer(10);
            final WebSocketSession out = question.getUser().getSession();

            @Override
            public void accept(ChatResponse chatResponse) {
                try {
                    Answer answer = Answer.builder()
                            .user(question.getUser())
                            .type(Answer.TYPE_ANSWER)
                            .content(chatResponse.getResult().getOutput().getText())
                            .build();
                    out.sendMessage(new TextMessage(Utils.serialize(answer)));
                } catch (Exception e) {
                    logger.error("ollama cll error", e);
                }
            }
        });
    }

    @Override
    public boolean support(Question question) {
        return MODEL_NAME.equals(question.getUser().getModel());
    }
}
