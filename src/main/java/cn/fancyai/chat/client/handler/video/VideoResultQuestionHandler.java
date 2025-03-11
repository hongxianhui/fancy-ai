package cn.fancyai.chat.client.handler.video;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.ChatUsage;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisOutput;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(320)
public class VideoResultQuestionHandler extends AbstractVideoQuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(VideoResultQuestionHandler.class);

    private String getTaskId(String content) {
        return content.substring(content.indexOf("：") + 1);
    }

    @Override
    protected String getModelName(Question question) {
        String content = question.getContent();
        if (content.contains("（wanx2.1-i2v-turbo）")) {
            return "wanx2.1-i2v-turbo";
        }
        if (content.contains("（wanx2.1-i2v-plus）")) {
            return "wanx2.1-i2v-plus";
        }
        return null;
    }

    @Override
    protected Object checkQuestion(Question question) {
        if (!question.getContent().startsWith("查询视频生成任务状态")) {
            return Boolean.FALSE;
        }
        String taskId = getTaskId(question.getContent());
        if (Strings.isBlank(taskId) || taskId.length() != 36) {
            return Answer.builder(question.getUser())
                    .content("查询格式不正确，请不要修改提示词前缀，并提供正确的任务ID。")
                    .done()
                    .build();
        }
        return Boolean.TRUE;
    }

    @Override
    protected Answer call(Question question, HandlerContext context) throws NoApiKeyException, InputRequiredException, JsonProcessingException {
        User user = question.getUser();
        VideoSynthesis videoSynthesis = new VideoSynthesis();
        VideoSynthesisResult result = videoSynthesis.fetch(getTaskId(question.getContent()), ChatUtils.getApiKey(user));
        String taskId = getTaskId(question.getContent());
        VideoSynthesisOutput output = result.getOutput();
        String taskStatus = output.getTaskStatus();
        logger.info("taskId: {}, taskStatus: {}", taskId, taskStatus);
        if ("SUCCEEDED".equals(taskStatus)) {
            context.unmute();
            user.getModel().setTool(getModelName(question));
            return Answer.builder(user)
                    .type(Answer.TYPE_VIDEO)
                    .content(ChatUtils.serialize(output))
                    .usage(ChatUsage.builder().user(user).videoDuration(5).build())
                    .done()
                    .build();
        }
        if ("FAILED".equals(taskStatus)) {
            context.unmute();
            return Answer.builder(user)
                    .type(Answer.TYPE_ANSWER)
                    .content("视频生成失败：" + output.getMessage())
                    .done()
                    .build();
        }
        context.mute();
        return Answer.builder(user)
                .type(Answer.TYPE_ANSWER)
                .content("任务还在执行中，点击任务ID可再次查询" +
                        "<span class=\"token splitter\"></span>" +
                        "<a href=\"javascript:$('#message-input').val('查询视频生成任务状态（" + getModelName(question) + "）：" + taskId + "')\">" + taskId + "</a>")
                .done()
                .build();
    }

}
