package cn.fancyai.chat.client.handler.text;

import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import org.springframework.ai.ResourceUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(530)
public class QWenPlusQuestionHandlerAbstract extends AbstractTextQuestionHandler {
    public static final String MODEL_NAME = "qwen-plus";

    @Override
    protected String getModelName(Question question) {
        return MODEL_NAME;
    }

    @Override
    protected List<Message> getSystemMessage(Question question) {
        return List.of(Message.builder().role(Role.SYSTEM.getValue())
                .content(ResourceUtils.getText("classpath:prompt/qwenplus-identity.txt"))
                .build());
    }

    @Override
    protected void customizeGenerationParam(Question question, GenerationParam.GenerationParamBuilder<?, ?> builder) {
        builder.enableSearch(true);
    }

    @Override
    protected Answer getAnswerOnStream(GenerationResult token, Question question) {
        GenerationOutput.Choice choice = token.getOutput().getChoices().getFirst();
        String content = choice.getMessage().getContent();
        Answer.Builder builder = Answer.builder().user(question.getUser()).type(Answer.TYPE_ANSWER).content(content);
        if (!choice.getFinishReason().isEmpty() && !"null".equals(choice.getFinishReason())) {
            builder.done();
        }
        return builder.build();
    }

}
