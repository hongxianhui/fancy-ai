package com.fancy.aichat.endpoint;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fancy.aichat.client.QuestionHandler;
import com.fancy.aichat.manager.UserManager;
import com.fancy.aichat.objects.Answer;
import com.fancy.aichat.objects.Question;
import com.fancy.aichat.objects.User;
import com.fancy.aichat.objects.Utils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WebSocketEndpoint extends AbstractWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEndpoint.class);

    @Resource
    private List<QuestionHandler> questionHandlers;
    @Resource
    private UserManager userManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("websocket session established , id is {}", session.getId());
        User user = userManager.userConnected(session);
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        if (StringUtils.hasText(query)) {
            Map<String, String> parameters = Utils.parseQueryParams(query);
            String apiKey = parameters.get("apiKey");
            if (StringUtils.hasText(apiKey)) {
                user.setApiKey(apiKey);
            }
        }
        Answer answer = Answer.builder().user(user).content(ResourceUtils.getText("classpath:constant/welcome.html")).done(true).build();
        session.sendMessage(new TextMessage(Utils.serialize(answer)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("User gone {}", session.getId());
        userManager.userGone(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User user = userManager.getUser(session.getId());
        try {
            Question question = Utils.deserialize(message.getPayload(), Question.class);
            question.setUser(user);
            logger.info("Question: {}", Utils.serialize(question));
            for (QuestionHandler handler : questionHandlers) {
                if (handler.handle(question)) {
                    break;
                }
            }
        } catch (NoApiKeyException e) {
            logger.error(e.getMessage());
            Answer answer = Answer.builder()
                    .user(user)
                    .type(Answer.TYPE_TOOL)
                    .content("""
                            未提供API-KEY，无法使用付费模型。
                            
                            请按如下格式在URL上提供阿里云百练大模型的API-KEY：
                            
                            <span style='color:blue'>http://fancy-ai.cn?apiKey=xxx。</span>
                            
                            做为开源项目，我们保证不会以任何形式截获和使用您的API-KEY。""")
                    .done(true)
                    .build();
            session.sendMessage(new TextMessage(Utils.serialize(answer)));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Websocket failed.", exception);
    }

}
