package cn.fancyai.chat.endpoint;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.client.handler.exception.ChatExceptionConsumer;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.util.Strings;
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

public class ChatEndpoint extends AbstractWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatEndpoint.class);

    @Resource
    private List<QuestionHandler> questionHandlers;
    @Resource
    private UserManager userManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        Map<String, String> parameters = ChatUtils.parseQueryParams(query);
        String userId = parameters.get("id");
        String apiKey = parameters.get("apiKey");
        if (Strings.isBlank(userId)) {
            logger.warn("User id is empty.");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("User id is empty."));
            return;
        }
        logger.info("Chat session established {}", userId);
        User user = userManager.userConnected(userId);
        user.setChatSession(new OutgoingSessionDecorator(session));
        if (StringUtils.hasText(apiKey)) {
            user.setApiKey(apiKey);
        }
        Answer answer = Answer.builder(user).content(ResourceUtils.getText("classpath:constant/" + user.getModel().getChat().replaceAll(":", "_") + ".html")).done().build();
        session.sendMessage(new TextMessage(ChatUtils.serialize(answer)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        Map<String, String> parameters = ChatUtils.parseQueryParams(query);
        String userId = parameters.get("id");
        logger.info("Chat user gone {}", userId);
        userManager.userGone(userId);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        Map<String, String> parameters = ChatUtils.parseQueryParams(query);
        String userId = parameters.get("id");
        User user = userManager.getUser(userId);
        try {
            HandlerContext context = new HandlerContext();
            Question question = ChatUtils.deserialize(message.getPayload(), Question.class);
            question.setUser(user);
            String log = ChatUtils.serialize(question);
            logger.info("Question: {}", log.substring(0, Math.min(log.length(), 500)));
            for (QuestionHandler handler : questionHandlers) {
                if (handler.handle(question, context)) {
                    break;
                }
            }
        } catch (Exception e) {
            new ChatExceptionConsumer(user).accept(e);
        }
    }

}
