package com.fancy.aichat.client.handler;

import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.fancy.aichat.objects.Answer;
import com.fancy.aichat.objects.Question;
import org.springframework.ai.ResourceUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Order(40)
public class DeepSeekR1QuestionHandler extends APIQuestionHandler {
    public static final String MODEL_NAME = "deepseek-r1";

    @Override
    protected String getModelName() {
        return MODEL_NAME;
    }

    @Override
    protected List<Message> getUserMessages(Question question) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder().role(Role.USER.getValue())
                .content(ResourceUtils.getText("classpath:prompt/deepseek-identity.txt"))
                .build());
        messages.addAll(super.getUserMessages(question));
        return messages;
    }

    @Override
    protected Answer getAnswerOnStream(GenerationResult token, Question question) {
        GenerationOutput.Choice choice = token.getOutput().getChoices().get(0);
        String reasoning = choice.getMessage().getReasoningContent();
        String content = choice.getMessage().getContent();
        Map<String, Object> metadata = question.getMetadata();
        String finishReason = choice.getFinishReason();
        if ("null".equals(finishReason)) {
            finishReason = null;
        }
        if (Boolean.TRUE.equals(metadata.get(Question.META_NO_THINK)) && content.isBlank() && finishReason == null) {
            return null;
        }
        Answer.AnswerBuilder builder = Answer.builder().user(question.getUser()).type(Answer.TYPE_THINK).content(reasoning);
        if (!content.isBlank()) {
            builder.type(Answer.TYPE_ANSWER);
            if (Boolean.TRUE.equals(metadata.get(Question.META_IS_THINKING))) {
                metadata.put(Question.META_IS_THINKING, false);
                if (!Boolean.TRUE.equals(metadata.get(Question.META_NO_THINK))) {
                    content = "\n\n" + content;
                }
            }
            builder.content(content);
        }
        if (finishReason != null) {
            builder.done(true);
        }
        return builder.build();
    }

}
