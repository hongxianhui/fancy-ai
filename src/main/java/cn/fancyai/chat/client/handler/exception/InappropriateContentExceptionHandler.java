package cn.fancyai.chat.client.handler.exception;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.text.SystemQuestionHandler;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

@Component
@Order(10)
public class InappropriateContentExceptionHandler implements ChatExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SystemQuestionHandler.class);

    @Override
    public void handle(User user, Throwable e) {
        try {
            Answer answer = Answer.builder(user).type(Answer.TYPE_TOOL).content("调用API失败，提示词或生成的文案中包括敏感词。").done().build();
            String answerJson = ChatUtils.serialize(answer);
            logger.info("Answer: {}", answerJson.substring(0, Math.min(answerJson.length(), 500)));
            logger.info("Cost: {}", ChatUtils.serialize(answer.getUsage()));
            user.getChatSession().sendMessage(new TextMessage(answerJson));
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean accept(Throwable e) {
        if (e instanceof ApiException) {
            String errorMessage = e.getMessage();
            return StringUtils.hasText(errorMessage) && errorMessage.contains("Input data may contain inappropriate content");
        }
        return false;
    }
}
