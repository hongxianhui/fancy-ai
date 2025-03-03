package cn.fancyai.chat.endpoint;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.objects.User;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Objects;

public class SpeechEndpoint extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SpeechEndpoint.class);

    @Resource
    private UserManager userManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        Map<String, String> parameters = ChatUtils.parseQueryParams(query);
        String userId = parameters.get("id");
        if (Strings.isBlank(userId)) {
            logger.warn("User id is empty.");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("User id is empty."));
            return;
        }
        logger.info("Speech session established {}", userId);
        User user = userManager.userConnected(userId);
        user.getChatSession().setSpeechSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        Map<String, String> parameters = ChatUtils.parseQueryParams(query);
        String userId = parameters.get("id");
        logger.info("Speech user gone {}", userId);
    }

}
