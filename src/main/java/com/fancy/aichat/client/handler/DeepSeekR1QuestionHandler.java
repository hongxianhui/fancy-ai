package com.fancy.aichat.client.handler;

import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.fancy.aichat.common.Answer;
import com.fancy.aichat.common.Question;
import org.apache.logging.log4j.util.Strings;
import org.springframework.ai.ResourceUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(2)
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
                .content(ResourceUtils.getText("classpath:prompt/api-identity.txt"))
                .build());
        messages.addAll(super.getUserMessages(question));
        return messages;
    }

    @Override
    protected Answer onStreamToken(GenerationResult token, Question question) {
        GenerationOutput.Choice choice = token.getOutput().getChoices().get(0);
        String reasoning = choice.getMessage().getReasoningContent();
        String content = choice.getMessage().getContent();
        Answer.Builder builder = Answer.builder().user(question.getUser()).type(Answer.TYPE_THINK).content(reasoning);
        if (!content.isBlank()) {
            builder.type(Answer.TYPE_ANSWER);
            builder.content(content);
        }
        if (Strings.isNotBlank(choice.getFinishReason()) || !"null".equals(choice.getFinishReason())) {
            builder.done();
            logger.info("Answer complete.");
        }
        return builder.build();
    }

}
