package cn.fancyai.chat.client.handler.question;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.client.handler.answer.AnswerCallback;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.Usage;
import cn.fancyai.chat.objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ResourceUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@Order(10)
public class SystemQuestionHandler implements QuestionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SystemQuestionHandler.class);

    @Override
    public boolean handle(Question question, AnswerCallback callback) throws Exception {
        String content = question.getContent().toLowerCase();
        User user = question.getUser();
        if (content.equals("切换角色")) {
            logger.info("Handler: {}, Question: {}", getClass().getName(), ChatUtils.serialize(question));
            Answer answer = Answer.builder()
                    .user(question.getUser())
                    .type(Answer.TYPE_TOOL)
                    .content(ResourceUtils.getText("classpath:/constant/roles.html"))
                    .done(true)
                    .build();
            callback.onAnswer(answer);
            return true;
        }
        if (content.equals("切换角色小欧")) {
            user.setModel(OllamaQuestionHandler.MODEL_NAME);
            sendModelWelcomeMessage(user, callback);
            return true;
        }
        if (content.equals("切换角色小千")) {
            user.setModel(QWenPlusQuestionHandlerAbstract.MODEL_NAME);
            sendModelWelcomeMessage(user, callback);
            return true;
        }
        if (content.equals("切换角色小迪")) {
            user.setModel(DeepSeekR1QuestionHandlerAbstract.MODEL_NAME);
            sendModelWelcomeMessage(user, callback);
            return true;
        }
        if (content.equals("切换角色小威")) {
            user.setModel(ImageQuestionHandler.MODEL_NAME);
            sendModelWelcomeMessage(user, callback);
            return true;
        }
        return false;
    }

    private void sendModelWelcomeMessage(User user, AnswerCallback callback) throws Exception {
        Answer answer = Answer.builder()
                .user(user)
                .type(Answer.TYPE_TOOL)
                .content(ResourceUtils.getText("classpath:/constant/" + user.getModel().replaceAll(":", "_") + ".html"))
                .done(true)
                .build();
        callback.onAnswer(answer);
    }
}
