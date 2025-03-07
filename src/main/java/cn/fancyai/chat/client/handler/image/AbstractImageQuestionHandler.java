package cn.fancyai.chat.client.handler.image;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.ChatUsage;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractImageQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(AbstractImageQuestionHandler.class);

    protected abstract ImageSynthesisResult call(Question question) throws NoApiKeyException, IOException, UploadFileException;

    protected abstract String getModelName(Question question);

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
        user.getModel().setTool(getModelName(question));
        logger.info("Handle question: {}::{}", getClass().getSimpleName(), user.getModel().getTool());
        ImageSynthesisResult result = call(question);
        if (!"SUCCEEDED".equals(result.getOutput().getTaskStatus())) {
            Answer answer = Answer.builder(user)
                    .content("图片生成失败，请稍后重试。")
                    .done()
                    .build();
            user.getChatSession().sendMessage(answer, context);
            return true;
        }
        List<Map<String, String>> url = result.getOutput().getResults();
        Answer answer = Answer.builder(user)
                .type(Answer.TYPE_IMAGE)
                .content(ChatUtils.serialize(url))
                .usage(ChatUsage.builder().user(user).imageAmount(1).build())
                .done()
                .build();
        user.getChatSession().sendMessage(answer, context);
        return true;
    }
}
