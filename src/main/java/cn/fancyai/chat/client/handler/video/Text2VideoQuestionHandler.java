package cn.fancyai.chat.client.handler.video;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(310)
public class Text2VideoQuestionHandler extends AbstractVideoQuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(Text2VideoQuestionHandler.class);

    @Override
    protected String getModelName(Question question) {
        String content = question.getContent();
        if (content.contains("（wanx2.1-t2v-turbo）")) {
            return "wanx2.1-t2v-turbo";
        }
        if (content.contains("（wanx2.1-t2v-plus）")) {
            return "wanx2.1-t2v-plus";
        }
        return null;
    }

    @Override
    protected Object checkQuestion(Question question) {
        if (!question.getContent().startsWith("文生视频")) {
            return Boolean.FALSE;
        }
        if (getModelName(question) == null) {
            return Answer.builder(question.getUser())
                    .content("模型名称不正确，请按提示词模板发送消息，不要删掉提示词前缀和模型名称。")
                    .done()
                    .build();
        }
        return Boolean.TRUE;
    }

    @Override
    protected Answer call(Question question, HandlerContext context) throws NoApiKeyException, InputRequiredException {
        User user = question.getUser();
        VideoSynthesis videoSynthesis = new VideoSynthesis();
        VideoSynthesisParam param = VideoSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(getModelName(question))
                .prompt(question.getContent())
                .extendPrompt(true)
                .build();
        VideoSynthesisResult result = videoSynthesis.asyncCall(param);
        String taskId = result.getOutput().getTaskId();
//        String taskId = "c7e4fb75-754b-407f-8e51-3650180aef47";
        return Answer.builder(user)
                .type(Answer.TYPE_ANSWER)
                .content("任务已创建，请稍候（5-10分钟）点击任务ID查询" +
                        "<span class=\"token splitter\"></span>" +
                        "<a href=\"javascript:$('#message-input').val('查询视频生成任务状态：" + taskId + "')\">" + taskId + "</a>")
                .done()
                .build();
    }
}
