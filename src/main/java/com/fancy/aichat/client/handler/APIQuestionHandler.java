package com.fancy.aichat.client.handler;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.fancy.aichat.client.QuestionHandler;
import com.fancy.aichat.common.Answer;
import com.fancy.aichat.common.Question;
import com.fancy.aichat.common.Utils;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class APIQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;
    @Getter
    private final Generation generation = new Generation();

    protected abstract String getModelName();

    protected abstract Answer onStreamToken(GenerationResult token, Question question);

    protected void customizeGenerationParam(Question question, GenerationParam.GenerationParamBuilder<?, ?> builder) {
    }

    protected List<Message> getSystemMessage(Question question) {
        return Collections.EMPTY_LIST;
    }

    protected List<Message> getUserMessages(Question question) {
        return List.of(Message.builder().role(Role.USER.getValue()).content(question.getContent()).build());
    }

    private GenerationParam getGenerationParam(Question question) {
        List<Message> messages = new ArrayList<>();
        List<Message> systemMessage = getSystemMessage(question);
        List<Message> userMessages = getUserMessages(question);
        if (systemMessage != null) {
            messages.addAll(systemMessage);
        }
        messages.addAll(userMessages);
        GenerationParam.GenerationParamBuilder<?, ?> builder = GenerationParam.builder()
                .apiKey(apiKey)
                .model(getModelName())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true);
        customizeGenerationParam(question, builder);
        return builder.build();
    }

    @Override
    public void handle(Question question) throws Exception {
        WebSocketSession output = question.getUser().getSession();
        if (!question.getUser().isAdmin()) {
            Answer answer = Answer.builder().user(question.getUser()).content("站长资金有限，匿名用户无法调用付费线上大模型。").done().build();
            output.sendMessage(new TextMessage(Utils.serialize(answer)));
            return;
        }
        Flowable<GenerationResult> result = generation.streamCall(getGenerationParam(question));
        result.forEach(new Consumer<>() {
            boolean thinking = false;

            @Override
            public void accept(GenerationResult token) throws Exception {
                try {
                    Answer answer = onStreamToken(token, question);
                    if (Answer.TYPE_THINK.equals(answer.getType())) {
                        thinking = true;
                    } else if (thinking) {
                        thinking = false;
                        answer.setContent("\n\n" + answer.getContent());
                    }
                    output.sendMessage(new TextMessage(Utils.serialize(answer)));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public boolean support(Question question) {
        return getModelName().equals(question.getUser().getModel());
    }
}

