package cn.fancyai.chat.client.handler.image;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.Usage;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractImageQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract ImageSynthesisResult call(Question question) throws NoApiKeyException, IOException, UploadFileException;

    protected Object checkQuestion(Question question) {
        return false;
    }

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
        context.mute();
        logger.info("Handler: {}, Model: {}", getClass().getSimpleName(), user.getModel().getImage());
        ImageSynthesisResult result = null;
        try {
            result = call(question);
        } catch (ApiException e) {
            logger.error(e.getMessage(), e);
            Answer answer = Answer.builder()
                    .user(user)
                    .content("图片生成失败，请稍后重试。")
                    .done()
                    .build();
            user.getChatSession().sendMessage(answer, context);
            return true;
        }
        if (!"SUCCEEDED".equals(result.getOutput().getTaskStatus())) {
            Answer answer = Answer.builder()
                    .user(user)
                    .content("图片生成失败，请稍后重试。")
                    .done()
                    .build();
            user.getChatSession().sendMessage(answer, context);
            return true;
        }
        List<Map<String, String>> url = result.getOutput().getResults();
        logger.info("Answer: {}", url);
        Answer answer = Answer.builder()
                .user(user)
                .type(Answer.TYPE_IMAGE)
                .content(ChatUtils.serialize(url))
                .usage(Usage.builder().user(user).imageAmount(1).build())
                .done()
                .build();
        user.getChatSession().sendMessage(answer, context);
        return true;
    }
}
