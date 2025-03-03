package cn.fancyai.chat.endpoint;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.AnswerHandler;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.objects.Answer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketSessionDecorator;

import java.util.Collection;

@Setter
@Getter
public class SpeechSessionDecorator extends WebSocketSessionDecorator {
    private final Collection<AnswerHandler> answerHandlers;
    private WebSocketSession speechSession;

    public SpeechSessionDecorator(WebSocketSession chatSession) {
        super(chatSession);
        answerHandlers = ServerApplication.applicationContext.getBeansOfType(AnswerHandler.class).values();
    }

    public void sendMessage(Answer answer, HandlerContext context) throws Exception {
        context.setSpeechSession(speechSession);
        for (AnswerHandler handler : answerHandlers) {
            if (handler.handle(answer, context)) {
                break;
            }
        }
        super.sendMessage(new TextMessage(ChatUtils.serialize(answer)));
    }
}
