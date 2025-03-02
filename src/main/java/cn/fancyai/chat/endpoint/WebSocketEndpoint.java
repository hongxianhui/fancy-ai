package cn.fancyai.chat.endpoint;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.client.handler.answer.AnswerCallback;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.exception.NoApiKeyException;
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
            Map<String, String> parameters = ChatUtils.parseQueryParams(query);
            String apiKey = parameters.get("apiKey");
            if (StringUtils.hasText(apiKey)) {
                user.setApiKey(apiKey);
            }
        }
        Answer answer = Answer.builder()
                .user(user)
                .content(ResourceUtils.getText("classpath:constant/" + user.getModel().replaceAll(":", "_") + ".html"))
                .done(true)
                .build();
        session.sendMessage(new TextMessage(ChatUtils.serialize(answer)));
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
            Question question = ChatUtils.deserialize(message.getPayload(), Question.class);
            question.setUser(user);
            logger.info("Question: {}", ChatUtils.serialize(question));
            for (QuestionHandler handler : questionHandlers) {
                if (handler.handle(question, new AnswerCallback(session))) {
                    break;
                }
            }
        } catch (NoApiKeyException e) {
            logger.error(e.getMessage());
            Answer answer = Answer.builder()
                    .user(user)
                    .type(Answer.TYPE_TOOL)
                    .content("未提供API-KEY，无法使用付费模型。" +
                            "\n\n请按如下格式在URL上提供阿里云百练大模型的API-KEY：" +
                            "\n\n<span style='color:blue'>http://fancy-ai.cn?apiKey=xxx。</span>" +
                            "\n\n做为开源项目，我们保证不会以任何形式截获和使用您的API-KEY。"
                    )
                    .done(true)
                    .build();
            session.sendMessage(new TextMessage(ChatUtils.serialize(answer)));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Websocket failed.", exception);
    }

}
