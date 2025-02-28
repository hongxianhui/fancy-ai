package com.fancy.aichat.client.handler;

import com.fancy.aichat.client.QuestionHandler;
import com.fancy.aichat.objects.Answer;
import com.fancy.aichat.objects.Question;
import com.fancy.aichat.objects.User;
import com.fancy.aichat.objects.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@Order(10)
public class SystemQuestionHandler implements QuestionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SystemQuestionHandler.class);

    @Override
    public boolean handle(Question question) throws IOException {
        String content = question.getContent().toLowerCase();
        User user = question.getUser();
        WebSocketSession output = user.getSession();
        if (content.contains("切换") || content.contains("召唤")) {
            logger.info("Handler: {}, Question: {}", getClass().getName(), Utils.serialize(question));
            if (!user.isAdmin()) {
                Answer answer = Answer.builder().user(user).content("站长囊中羞涩，匿名用户无法切换付费模型。").done(true).build();
                output.sendMessage(new TextMessage(Utils.serialize(answer)));
                return true;
            }
            if (content.contains("小欧") || content.contains("小o")) {
                user.setModel(OllamaQuestionHandler.MODEL_NAME);
                question.setContent("你好");
            }
            if (content.contains("小千") || content.contains("千问") || content.contains("千千")) {
                user.setModel(QWenPlusQuestionHandler.MODEL_NAME);
                question.setContent("你好");
            }
            if (content.contains("小迪") || content.contains("deepseek") || content.contains("迪迪")) {
                user.setModel(DeepSeekR1QuestionHandler.MODEL_NAME);
                Answer answer = Answer.builder().user(user).content("你好，我是小迪。").done(true).build();
                output.sendMessage(new TextMessage(Utils.serialize(answer)));
                return true;
            }
            return false;
        }
        return false;
    }
}
