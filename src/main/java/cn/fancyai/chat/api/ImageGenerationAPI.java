package cn.fancyai.chat.api;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.objects.ChatUsage;
import cn.fancyai.chat.objects.UsageBase;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ImageGenerationAPI {
    protected final Logger logger = LoggerFactory.getLogger(ImageGenerationAPI.class);

    public String generate(User user, String userPrompt, ChatUsage chatUsage) throws Exception {
        logger.info("Call API: {}::{}::{}", getClass().getSimpleName(), user.getModel().getTool(), userPrompt);
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(user.getModel().getTool())
                .prompt(userPrompt)
                .size("576*1024")
                .n(1)
                .build();
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult synthesisResult = imageSynthesis.call(param);
//        System.out.println(ChatUtils.serialize(synthesisResult));
        List<Map<String, String>> url = synthesisResult.getOutput().getResults();
        UsageBase calculator = new UsageBase();
        float answerFee = calculator.getImageAnswerAmountFee(user.getModel().getTool(), 1);
        chatUsage.setFee(chatUsage.getFee() + answerFee);
        return url.get(0).get("url");
    }
}
