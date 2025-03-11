package cn.fancyai.chat.api;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.exception.ChatExceptionConsumer;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.ChatUsage;
import cn.fancyai.chat.objects.UsageBase;
import cn.fancyai.chat.objects.User;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class TextGenerationAPI {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChatModel chatModel;

    public TextGenerationAPI(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String generate(User user, String userPrompt, String systemPrompt, ChatUsage chatUsage) throws Exception {
        logger.info("Call API: {}", getClass().getSimpleName());
        DashScopeChatOptions.DashscopeChatOptionsBuilder chatOptionsBuilder = DashScopeChatOptions.builder().withModel(user.getModel().getTool());
        DashScopeChatModel chatModel = new DashScopeChatModel(new DashScopeApi(ChatUtils.getApiKey(user)), chatOptionsBuilder.build());
        Prompt prompt = new Prompt(new UserMessage(systemPrompt), new UserMessage("严格按照要求生成符合以下论点的短视频文案：" + userPrompt));
        StringBuffer result = new StringBuffer();
        CountDownLatch latch = new CountDownLatch(1);
        chatModel.stream(prompt).subscribe(chatResponse -> {
            String content = chatResponse.getResult().getOutput().getText();
            if (StringUtils.hasText(content) && content.contains("{换行}")) {
                content = content.replace("{换行}", "\n").replaceAll("【.*?】", "");
            }
            Answer.Builder builder = Answer.builder(user).type(Answer.TYPE_FLOW).content(content);
            String finishReason = chatResponse.getResult().getMetadata().getFinishReason();
            if (finishReason != null && !"null".equalsIgnoreCase(finishReason)) {
                builder.done();
                Usage usage = chatResponse.getMetadata().getUsage();
                UsageBase calculator = new UsageBase();
                float questionFee = calculator.getQuestionTokenFee(user.getModel().getTool(), usage.getPromptTokens().intValue());
                float answerFee = calculator.getSpeechFee(user.getModel().getTool(), usage.getTotalTokens().intValue());
                chatUsage.setFee(chatUsage.getFee() + questionFee + answerFee);
            }
            Answer answer = builder.build();
            try {
                user.getChatSession().sendMessage(new TextMessage(ChatUtils.serialize(answer)));
            } catch (IOException e) {
                //ignore
            }
            result.append(content);
            if (answer.isDone()) {
                latch.countDown();
            }
        }, new ChatExceptionConsumer(user));
        if (!latch.await(90, TimeUnit.SECONDS)) {
            user.getChatSession().sendMessage(new TextMessage(ChatUtils.serialize(Answer.builder(user).content("模型输出超时，请稍候再试。").done().build())));
        }
        return result.toString();
    }
}

