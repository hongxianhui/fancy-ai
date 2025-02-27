package com.fancy.aichat.endpoint;

import com.fancy.aichat.client.QuestionHandler;
import com.fancy.aichat.common.Answer;
import com.fancy.aichat.common.Question;
import com.fancy.aichat.common.User;
import com.fancy.aichat.common.Utils;
import com.fancy.aichat.manager.UserManager;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ResourceUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.List;

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
        Answer answer = Answer.builder().user(user).content(ResourceUtils.getText("classpath:constant/welcome.html")).done().build();
        session.sendMessage(new TextMessage(Utils.serialize(answer)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("User gone {}", session.getId());
        userManager.userGone(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Question question = Utils.deserialize(message.getPayload(), Question.class);
        question.setUser(userManager.getUser(session.getId()));
        logger.info("Question: {}", Utils.serialize(question));
        handleQuestion(question);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Websocket failed.", exception);
    }

    private void handleQuestion(Question question) {
        for (QuestionHandler handler : questionHandlers) {
            if (handler.support(question)) {
                try {
                    handler.handle(question);
                    return;
                } catch (Exception e) {
                    logger.error("Client: handle question failed.", e);
                }
            }
        }
        Answer answer = Answer.builder().user(question.getUser()).content("调用的模型不存在：" + question.getUser().getModel()).done().build();
    }

}
