package com.fancy.aichat.client.handler;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.GenerationUsage;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.fancy.aichat.client.QuestionHandler;
import com.fancy.aichat.client.tokenizer.ChatStreamTokenizer;
import com.fancy.aichat.endpoint.TTSEndpoint;
import com.fancy.aichat.objects.*;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class APIQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Resource
    private TTSEndpoint ttsEndpoint;

    private final Generation generation = new Generation();

    protected abstract String getModelName();

    protected abstract Answer getAnswerOnStream(GenerationResult token, Question question);

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
    public boolean handle(Question question) throws Exception {
        User user = question.getUser();
        if (!user.getModel().equals(getModelName())) {
            return false;
        }
        logger.info("Handler: {}, Question: {}", getClass().getName(), Utils.serialize(question));
        WebSocketSession output = user.getSession();
        if (!user.isAdmin()) {
            Answer answer = Answer.builder().content("站长资金有限，匿名用户无法调用付费线上大模型。").done(true).build();
            output.sendMessage(new TextMessage(Utils.serialize(answer)));
            return true;
        }
        question.getMetadata().put(Question.META_IS_THINKING, true);
        ChatStreamTokenizer tokenizer = new ChatStreamTokenizer(10);
        AtomicInteger voiceTokens = new AtomicInteger();
        generation.streamCall(getGenerationParam(question)).forEach(token -> {
            try {
                Answer answer = getAnswerOnStream(token, question);
                if (answer == null) {
                    return;
                }
                if (Boolean.TRUE.equals(question.getUser().getMetadata().get(User.META_VOICE))) {
                    String sentence = tokenizer.tokenize(answer.getContent());
                    if (StringUtils.hasText(sentence)) {
                        ttsEndpoint.transform(user, sentence);
                        voiceTokens.addAndGet(sentence.length());
                    }
                }
                if (answer.isDone()) {
                    String remaining = tokenizer.getRemaining();
                    if (StringUtils.hasText(remaining)) {
                        ttsEndpoint.transform(user, remaining);
                        voiceTokens.addAndGet(remaining.length());
                    }
                    GenerationUsage usage = token.getUsage();
                    Usage chatUsage = Usage.builder().question(question)
                            .completionTokens(usage.getTotalTokens())
                            .promptTokens(usage.getTotalTokens())
                            .voiceTokens(voiceTokens.get()).build();
                    answer.setUsage(chatUsage);
                    ttsEndpoint.done(user.getUserId());
                    logger.info("Answer complete, cost: {}", Utils.serialize(chatUsage));
                }
                output.sendMessage(new TextMessage(Utils.serialize(answer)));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
        return true;
    }
}

