package cn.fancyai.chat.client.handler.vl;

import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationOutput;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(490)
public class ImageVLQuestionHandler extends AbstractVLQuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected String getModelName(Question question) {
        String content = question.getContent();
        if (content.contains("（qwen-vl-max）")) {
            return "qwen-vl-max";
        }
        if (content.contains("（qwen-vl-plus）")) {
            return "qwen-vl-plus";
        }
        return null;
    }

    @Override
    protected Object checkQuestion(Question question) {
        if (!question.getContent().startsWith("理解图片内容")) {
            return Boolean.FALSE;
        }
        String imageModel = getModelName(question);
        if (imageModel == null) {
            return Answer.builder()
                    .user(question.getUser())
                    .content("模型不存在，请提供正确的模型名称。")
                    .done()
                    .build();
        }
        question.getUser().getModel().setImage(imageModel);
        return Boolean.TRUE;
    }

    @Override
    protected Answer getAnswerOnStream(MultiModalConversationResult result, Question question) {
        MultiModalConversationOutput.Choice choice = result.getOutput().getChoices().getFirst();
        String content = (String) choice.getMessage().getContent().getFirst().get("text");
        Answer.Builder builder = Answer.builder()
                .user(question.getUser())
                .type(Answer.TYPE_ANSWER)
                .content(content);
        if (!choice.getFinishReason().isEmpty() && !"null".equals(choice.getFinishReason())) {
            builder.done();
        }
        return builder.build();
    }
}
