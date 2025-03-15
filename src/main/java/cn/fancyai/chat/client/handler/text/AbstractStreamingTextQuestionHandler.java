package cn.fancyai.chat.client.handler.text;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.client.handler.exception.ChatExceptionConsumer;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.ChatUsage;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.dashscope.exception.NoApiKeyException;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractStreamingTextQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ChatMemory chatMemory;

    protected abstract String getModelName();

    protected void customizeChatOperations(Question question, DashScopeChatOptions.DashscopeChatOptionsBuilder builder) {
    }

    protected List<Advisor> additionalAdvisors(Question question) throws NoApiKeyException {
        return Collections.EMPTY_LIST;
    }

    protected List<FunctionCallback> additionalFunctions(Question question) {
        return Collections.EMPTY_LIST;
    }

    protected Answer getAnswerOnStream(ChatResponse chatResponse, Question question, HandlerContext context) {
        String content = chatResponse.getResult().getOutput().getText();
        Answer.Builder builder = Answer.builder(question.getUser()).type(Answer.TYPE_ANSWER).content(content);
        String finishReason = chatResponse.getResult().getMetadata().getFinishReason();
        if (StringUtils.hasText(finishReason) && !"null".equalsIgnoreCase(finishReason)) {
            builder.done();
        }
        return builder.build();
    }

    protected String getAPIKey(Question question) throws NoApiKeyException {
        return ChatUtils.getApiKey(question.getUser());
    }

    @Override
    public boolean handle(Question question, HandlerContext context) throws Exception {
        User user = question.getUser();
        if (!user.getModel().getChat().equals(getModelName())) {
            return false;
        }
        logger.info("Handle question: {}::{}", getClass().getSimpleName(), getModelName());
        //ChatOptions
        List<FunctionCallback> functionCallbacks = additionalFunctions(question);
        DashScopeChatOptions.DashscopeChatOptionsBuilder optionsBuilder = DashScopeChatOptions.builder()
                .withModel(getModelName())
                .withIncrementalOutput(true)
                .withFunctionCallbacks(functionCallbacks)
                .withFunctions(functionCallbacks.stream()
                        .map(FunctionCallback::getName)
                        .collect(Collectors.toSet()));
        customizeChatOperations(question, optionsBuilder);
        //ChatModel
        ChatModel chatModel = new DashScopeChatModel(new DashScopeApi(getAPIKey(question)),
                optionsBuilder.build());
        //Advisors
        List<Advisor> advisors = new ArrayList<>();
        advisors.add(MessageChatMemoryAdvisor.builder(chatMemory)
                .chatMemoryRetrieveSize(100)
                .conversationId(user.getUserId())
                .build());
        advisors.addAll(additionalAdvisors(question));
        //ChatClient
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(ChatUtils.getPrompt(getModelName() + "-system.txt"))
                .defaultAdvisors(advisors)
                .build();
        //Call
        chatClient.prompt(new Prompt(question.getContent()))
                .stream().chatResponse().subscribe(chatResponse -> {
            Answer answer = getAnswerOnStream(chatResponse, question, context);
            if (answer == null) {
                return;
            }
            if (!context.containsKey("previousAnswer")) {
                context.put("previousAnswer", answer);
                logger.info("Receiving answer stream tokens...");
            }
            if (answer.isDone()) {
                Usage usage = chatResponse.getMetadata().getUsage();
                ChatUsage chatUsage = ChatUsage.builder().user(user)
                        .promptTokens(usage.getPromptTokens().intValue())
                        .completionTokens(usage.getTotalTokens().intValue())
                        .build();
                answer.setUsage(chatUsage);
            }
            user.getChatSession().sendMessage(answer, context);
        }, new ChatExceptionConsumer(user));
        return true;
    }
}

