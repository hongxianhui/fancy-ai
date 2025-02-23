package com.fancy.aichat.server;

import org.fancy.aichat.common.ChatPrompt;
import org.fancy.aichat.common.Question;
import org.fancy.aichat.common.User;
import org.fancy.aichat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import javax.annotation.Resource;

@Component
public class WebSocketEndpoint extends AbstractWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEndpoint.class);

    @Resource
    private SocketServer proxy;
    @Resource
    private UserManager userManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ChatUser user = userManager.userConnected(session);
        proxy.ask(Question.builder().user(user).prompt(ChatPrompt.USER_CONNECTED).content(Utils.serialize(userManager.getUsers())).build());
        logger.info("websocket session established , id is {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Question question = Utils.deserialize(message.getPayload(), Question.class);
        question.setUser(userManager.getUser(session.getId()));
        logger.info("Question: {}", Utils.serialize(question));
        proxy.ask(question);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Websocket failed.", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        User user = userManager.userGone(session);
        proxy.ask(Question.builder().user(user).prompt(ChatPrompt.USER_GONE).content(Utils.serialize(userManager.getUsers())).build());
        logger.info("User gone {}", session.getId());
    }

}
