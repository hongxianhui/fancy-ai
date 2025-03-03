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
@Order(510)
public class GeneraImageQuestionHandler extends AbstractImageQuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String getImageModel(String content) {
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
        String imageModel = getImageModel(question.getContent());
        if (imageModel == null) {
            return Answer.builder()
                    .user(question.getUser())
                    .content(ChatUtils.getText("image-vl.html"))
                    .done()
                    .build();
        }
        question.getUser().getModel().setImage(imageModel);
        return Boolean.TRUE;
    }

    @Override
    protected ImageSynthesisResult call(Question question) throws NoApiKeyException {
        User user = question.getUser();
        String content = question.getContent();
        String model = getImageModel(content);
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(model)
                .prompt(content.substring(content.indexOf("：") + 1))
                .n(1)
                .build();
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        return imageSynthesis.call(param);
    }

}
