package cn.fancyai.chat.client.handler.question;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.client.handler.answer.AnswerCallback;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.Usage;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.GenerationUsage;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractTextQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

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

    private GenerationParam getGenerationParam(Question question) throws NoApiKeyException {
        List<Message> messages = new ArrayList<>();
        List<Message> systemMessage = getSystemMessage(question);
        List<Message> userMessages = getUserMessages(question);
        if (systemMessage != null) {
            messages.addAll(systemMessage);
        }
        messages.addAll(userMessages);
        GenerationParam.GenerationParamBuilder<?, ?> builder = GenerationParam.builder()
                .apiKey(ChatUtils.getApiKey(question.getUser()))
                .model(getModelName())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true);
        customizeGenerationParam(question, builder);
        return builder.build();
    }

    @Override
    public boolean handle(Question question, AnswerCallback callback) throws Exception {
        User user = question.getUser();
        if (!user.getModel().equals(getModelName())) {
            return false;
        }
        logger.info("Handler: {}, Question: {}", getClass().getName(), ChatUtils.serialize(question));
        question.getMetadata().put(Question.META_IS_THINKING, true);
        generation.streamCall(getGenerationParam(question)).blockingForEach(token -> {
            try {
                if (!callback.isOpen()) {
                    return;
                }
                Answer answer = getAnswerOnStream(token, question);
                if (answer == null) {
                    return;
                }
                if (answer.isDone()) {
                    GenerationUsage usage = token.getUsage();
                    Usage chatUsage = Usage.builder().user(user)
                            .completionTokens(usage.getTotalTokens())
                            .promptTokens(usage.getTotalTokens())
                            .build();
                    answer.setUsage(chatUsage);
                    logger.info("Answer complete, cost: {}", ChatUtils.serialize(chatUsage));
                }
                callback.onAnswer(answer);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
        return true;
    }
}

