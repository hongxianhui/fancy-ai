package cn.fancyai.chat.client.handler.video;

import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractVideoQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(AbstractVideoQuestionHandler.class);

    protected abstract Answer call(Question question, HandlerContext context) throws NoApiKeyException, InputRequiredException, JsonProcessingException;

    protected abstract String getModelName(Question question);

    protected abstract Object checkQuestion(Question question);

    @Override
    public boolean handle(Question question, HandlerContext context) throws Exception {
        Object checkResult = checkQuestion(question);
        if (Boolean.FALSE.equals(checkResult)) {
            return false;
        }
        User user = question.getUser();
        if (checkResult instanceof Answer) {
            user.getChatSession().sendMessage(((Answer) checkResult), context);
            return true;
        }
        logger.info("Handle question: {}::{}", getClass().getSimpleName(), getModelName(question));
        user.getModel().setTool(getModelName(question));
        Answer answer = call(question, context);
        user.getChatSession().sendMessage(answer, context);
        return true;
    }
}
