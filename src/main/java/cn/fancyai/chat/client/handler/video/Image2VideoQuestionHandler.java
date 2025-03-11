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
import org.springframework.util.StringUtils;

@Component
@Order(310)
public class Image2VideoQuestionHandler extends AbstractVideoQuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(Image2VideoQuestionHandler.class);

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
        if (!question.getContent().startsWith("图生视频")) {
            return Boolean.FALSE;
        }
        if (getModelName(question) == null) {
            return Answer.builder(question.getUser())
                    .content("模型名称不正确，请按提示词模板发送消息，不要修改提示词前缀和模型名称。")
                    .done()
                    .build();
        }
        try {
            String content = question.getContent();
            String prompt = content.substring(content.indexOf("提示词：") + 4);
            String imageName = content.substring(content.indexOf("图片：") + 3).substring(0, content.indexOf("\n"));
            if (!StringUtils.hasText(prompt) || !StringUtils.hasText(imageName)) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            return Answer.builder(question.getUser())
                    .content("提示词格式不正确，请按提示词模板发送消息，不要修改提示词前缀。")
                    .done()
                    .build();
        }
        return Boolean.TRUE;
    }

    @Override
    protected Answer call(Question question, HandlerContext context) throws NoApiKeyException, InputRequiredException {
        User user = question.getUser();
        String content = question.getContent();
        String prompt = content.substring(content.indexOf("提示词：") + 4);
        String imageName = content.substring(content.indexOf("图片：") + 3).substring(0, content.indexOf("\n"));
        VideoSynthesis videoSynthesis = new VideoSynthesis();
        VideoSynthesisParam param = VideoSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(getModelName(question))
                .imgUrl("http://fancy-ai.cn/download?fileName=" + imageName)
//                .imgUrl("http://fancy-ai.cn/image/b.jpg")
                .prompt(prompt)
                .size("720*1280")
                .extendPrompt(true)
                .build();
        VideoSynthesisResult result = videoSynthesis.asyncCall(param);
        String taskId = result.getOutput().getTaskId();
//        String taskId = "5ba7deb1-473e-42e8-a894-ee6a7a7b27b5";
        return Answer.builder(user)
                .type(Answer.TYPE_ANSWER)
                .content("任务已创建，请稍候（5-10分钟）点击任务ID查询" +
                        "<span class=\"token splitter\"></span>" +
                        "<a href=\"javascript:$('#message-input').val('查询视频生成任务状态（" + getModelName(question) + "）：" + taskId + "')\">" + taskId + "</a>")
                .done()
                .build();
    }
}
