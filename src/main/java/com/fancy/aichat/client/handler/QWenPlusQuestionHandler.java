package com.fancy.aichat.client.handler;

import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.fancy.aichat.objects.Answer;
import com.fancy.aichat.objects.Question;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(30)
public class QWenPlusQuestionHandler extends APIQuestionHandler {
    public static final String MODEL_NAME = "qwen-plus";

    @Override
    protected String getModelName() {
        return MODEL_NAME;
    }

    public QWenPlusQuestionHandler(ChatClient.Builder modelBuilder) {

    }

    @Override
    protected List<Message> getSystemMessage(Question question) {
        return List.of(Message.builder().role(Role.SYSTEM.getValue())
                .content(ResourceUtils.getText("classpath:prompt/qwen-identity.txt"))
                .build());
    }

    @Override
    protected void customizeGenerationParam(Question question, GenerationParam.GenerationParamBuilder<?, ?> builder) {
        builder.enableSearch(true);
    }

    @Override
    protected Answer getAnswerOnStream(GenerationResult token, Question question) {
        GenerationOutput.Choice choice = token.getOutput().getChoices().get(0);
        String content = choice.getMessage().getContent();
        Answer.AnswerBuilder builder = Answer.builder().user(question.getUser()).type(Answer.TYPE_ANSWER).content(content);
        if (!choice.getFinishReason().isEmpty() && !"null".equals(choice.getFinishReason())) {
            builder.done(true);
        }
        return builder.build();
    }

}
