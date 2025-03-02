package cn.fancyai.chat.client.handler.answer;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.AnswerHandler;
import cn.fancyai.chat.objects.Answer;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;

public class AnswerCallback {
    private final WebSocketSession session;
    private final Collection<AnswerHandler> answerHandlers;


    public AnswerCallback(WebSocketSession session) {
        this.session = session;
        answerHandlers = ServerApplication.applicationContext.getBeansOfType(AnswerHandler.class).values();
    }

    public void onAnswer(Answer answer) throws Exception {
        for (AnswerHandler handler : answerHandlers) {
            if (handler.handle(answer)) {
                break;
            }
        }
        session.sendMessage(new TextMessage(ChatUtils.serialize(answer)));
    }

    public boolean isOpen() {
        return session.isOpen();
    }
}
