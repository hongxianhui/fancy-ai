package cn.fancyai.chat.endpoint;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.exception.NoApiKeyException;
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
        user.setChatSession(new SpeechSessionDecorator(session));
        if (StringUtils.hasText(apiKey)) {
            user.setApiKey(apiKey);
        }
        Answer answer = Answer.builder()
                .user(user)
                .content(ResourceUtils.getText("classpath:constant/" + user.getModel().getChat().replaceAll(":", "_") + ".html"))
                .done()
                .build();
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
            logger.info("Question: {}", ChatUtils.serialize(question));
            for (QuestionHandler handler : questionHandlers) {
                if (handler.handle(question, context)) {
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
                            
                            
                            做为开源项目，我们保证不会以任何形式截获和使用您的API-KEY。"""
                    )
                    .done()
                    .build();
            session.sendMessage(new TextMessage(ChatUtils.serialize(answer)));
        }
    }

}
