package cn.fancyai.chat.endpoint;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.AnswerHandler;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.objects.Answer;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketSessionDecorator;

import java.util.Collection;

@Setter
@Getter
public class OutgoingSessionDecorator extends WebSocketSessionDecorator {
    private static final Logger logger = LoggerFactory.getLogger(OutgoingSessionDecorator.class);
    private final Collection<AnswerHandler> answerHandlers;
    private WebSocketSession speechSession;

    public OutgoingSessionDecorator(WebSocketSession chatSession) {
        super(chatSession);
        answerHandlers = ServerApplication.applicationContext.getBeansOfType(AnswerHandler.class).values();
    }

    public void sendMessage(Answer answer, HandlerContext context) {
        try {
            context.setSpeechSession(speechSession);
            for (AnswerHandler handler : answerHandlers) {
                if (handler.handle(answer, context)) {
                    break;
                }
            }
            String answerJson = ChatUtils.serialize(answer);
            if (answer.isDone()) {
                logger.info("Answer: {}", answerJson.substring(0, Math.min(answerJson.length(), 500)));
                logger.info("Cost: {}", ChatUtils.serialize(answer.getUsage()));
            }
            super.sendMessage(new TextMessage(answerJson));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
