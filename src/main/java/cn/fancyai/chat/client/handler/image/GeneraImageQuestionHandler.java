package cn.fancyai.chat.client.handler.image;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(420)
public class GeneraImageQuestionHandler extends AbstractImageQuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(GeneraImageQuestionHandler.class);

    @Override
    protected String getModelName(Question question) {
        String content = question.getContent();
        if (content.contains("（FLUX）")) {
            return "flux-dev";
        }
        if (content.contains("（wanx2.0-t2i-turbo）")) {
            return "wanx2.0-t2i-turbo";
        }
        if (content.contains("（wanx2.1-t2i-turbo）")) {
            return "wanx2.1-t2i-turbo";
        }
        if (content.contains("（wanx2.1-t2i-plus）")) {
            return "wanx2.1-t2i-plus";
        }
        if (content.contains("（stable-diffusion-3.5-large）")) {
            return "stable-diffusion-3.5-large";
        }
        if (content.contains("（stable-diffusion-3.5-large-turbo）")) {
            return "stable-diffusion-3.5-large-turbo";
        }
        return null;
    }

    @Override
    protected Object checkQuestion(Question question) {
        if (!question.getContent().startsWith("一键成图")) {
            return Boolean.FALSE;
        }
        if (getModelName(question) == null) {
            return Answer.builder(question.getUser())
                    .content(ChatUtils.getConstant("image-vl.html"))
                    .done()
                    .build();
        }
        return Boolean.TRUE;
    }

    @Override
    protected ImageSynthesisResult call(Question question) throws NoApiKeyException {
        User user = question.getUser();
        String content = question.getContent();
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(getModelName(question))
                .prompt(content.substring(content.indexOf("：") + 1))
                .n(1)
                .build();
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        return imageSynthesis.call(param);
    }

}
