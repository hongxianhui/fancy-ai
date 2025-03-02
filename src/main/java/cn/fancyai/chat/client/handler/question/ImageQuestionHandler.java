package cn.fancyai.chat.client.handler.question;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.client.handler.answer.AnswerCallback;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.Usage;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Order(50)
public class ImageQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    public static final String MODEL_NAME = "wanx2.1-t2i-turbo";

    @Override
    public boolean handle(Question question, AnswerCallback callback) throws Exception {
        User user = question.getUser();
        if (!user.getModel().equals(MODEL_NAME)) {
            return false;
        }
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(MODEL_NAME)
                .prompt(question.getContent())
                .n(1)
                .size("640*640")
                .build();
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult response = imageSynthesis.call(param);
        List<Map<String, String>> result = response.getOutput().getResults();
        logger.info("Answer: {}", ChatUtils.serialize(result));
        Answer answer = Answer.builder()
                .user(question.getUser())
                .type(Answer.TYPE_IMAGE)
                .content(ChatUtils.serialize(result))
//                .content("[{\"orig_prompt\":\"写实动物摄影，一只橘色小猫和白色小狗正在激烈打闹。小猫弓起背部，毛发竖立，露出尖锐牙齿，前爪试图扑向小狗。小狗则后腿站立，嘴巴张开欲咬，耳朵紧贴头部，尾巴高高翘起。两者动作定格在最具张力的瞬间。背景是温暖的午后庭院，阳光洒在地面形成自然光影。高清动态捕捉，近景特写，画面充满力量感与戏剧性冲突。\",\"actual_prompt\":\"写实动物摄影，一只橘色小猫和白色小狗正在激烈打闹。小猫弓起背部，毛发竖立，露出尖锐牙齿，前爪试图扑向小狗。小狗则后腿站立，嘴巴张开欲咬，耳朵紧贴头部，尾巴高高翘起。两者动作定格在最具张力的瞬间。背景是温暖的午后庭院，阳光洒在地面形成自然光影。高清动态捕捉，近景特写，画面充满力量感与戏剧性冲突。\",\"url\":\"https://dashscope-result-sh.oss-cn-shanghai.aliyuncs.com/1d/49/20250301/775d1686/4ef73132-89ef-4ae4-b366-1f9f6a57081f3261594598.png?Expires=1740913448&OSSAccessKeyId=LTAI5tKPD3TMqf2Lna1fASuh&Signature=o49OgS3333kleyoi2NCi69ThfDM%3D\"}]")
                .usage(Usage.builder().user(user).imageAmount(1).build())
                .done(true)
                .build();
        callback.onAnswer(answer);
        return true;
    }
}
