package cn.fancyai.chat.client.handler.vl;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.ChatUsage;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationUsage;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractVLQuestionHandler implements QuestionHandler {
    @Value("${ai.tempfile.folder}")
    private String tempFileFolder;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract String getModelName(Question question);

    protected abstract Answer getAnswerOnStream(MultiModalConversationResult result, Question question);

    protected Object checkQuestion(Question question) {
        return false;
    }

    protected void customizeGenerationParam(Question question, MultiModalConversationParam.MultiModalConversationParamBuilder<?, ?> builder) {
    }

    private MultiModalConversationParam getParameter(Question question) throws NoApiKeyException, IOException {
        Path imagePath = Path.of(tempFileFolder, question.getContent().substring(question.getContent().indexOf("：") + 1));
        User user = question.getUser();
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(Map.of("image", imagePath.toFile().toString()), Map.of("text", "图中描绘的是什么景象？")))
                .build();
        MultiModalConversationParam.MultiModalConversationParamBuilder<?, ?> builder = MultiModalConversationParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(user.getModel().getTool())
                .messages(Collections.singletonList(userMessage))
                .incrementalOutput(true);
        customizeGenerationParam(question, builder);
        return builder.build();
    }

    @Override
    public boolean handle(Question question, HandlerContext context) throws Exception {
        Object checkResult = checkQuestion(question);
        if (Boolean.FALSE.equals(checkResult)) {
            return false;
        }
        User user = question.getUser();
        if (checkResult instanceof Answer) {
            user.getChatSession().sendMessage((Answer) checkResult, context);
            return true;
        }
        logger.info("Handle question: {}::{}", getClass().getSimpleName(), getModelName(question));
        user.getModel().setTool(getModelName(question));
        MultiModalConversation multiModalConversation = new MultiModalConversation();
        multiModalConversation.streamCall(getParameter(question)).blockingForEach(result -> {
            try {
                Answer answer = getAnswerOnStream(result, question);
                if (answer == null) {
                    return;
                }
                if (answer.isDone()) {
                    MultiModalConversationUsage usage = result.getUsage();
                    ChatUsage chatUsage = ChatUsage.builder().user(user)
                            .completionTokens(usage.getOutputTokens())
                            .promptTokens(usage.getInputTokens())
//                            .imageTokens(usage.getImageTokens())
                            .build();
                    answer.setUsage(chatUsage);
                }
                user.getChatSession().sendMessage(answer, context);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
        return true;
    }
}

