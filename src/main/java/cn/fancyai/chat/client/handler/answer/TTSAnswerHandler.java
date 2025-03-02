package cn.fancyai.chat.client.handler.answer;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.AnswerHandler;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Usage;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(10)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TTSAnswerHandler implements AnswerHandler {
    private static final Logger logger = LoggerFactory.getLogger(TTSAnswerHandler.class);

    @Value("${spring.ai.dash-scope.audio.options.model}")
    private String model;
    @Value("${spring.ai.dash-scope.audio.options.voice}")
    private String voice;

    private final Map<String, OutputStream> streams = new ConcurrentHashMap<>();

    public void addVoiceListener(String userId, OutputStream out) {
        streams.put(userId, out);
    }

    @Override
    public boolean handle(Answer answer) throws NoApiKeyException, IOException {
        User user = answer.getUser();
        String userId = user.getUserId();
        OutputStream outputStream = streams.get(userId);
        if (!Boolean.TRUE.equals(user.getMetadata().get(User.META_VOICE))) {
            return false;
        }
        if (outputStream == null) {
            logger.warn("stream for user {} not found", userId);
            return false;
        }
        AtomicInteger ttsTokens = (AtomicInteger) answer.getMetadata().get(Answer.META_TTS_TOKENS);
        if (ttsTokens == null) {
            answer.getMetadata().put(Answer.META_TTS_TOKENS, ttsTokens = new AtomicInteger());
        }
        ttsTokens.addAndGet(answer.getContent().length());
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(SpeechSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(model)
                .voice(voice)
                .build(), new ResultCallback<>() {
            @Override
            public void onEvent(SpeechSynthesisResult result) {
                ByteBuffer audioFrame = result.getAudioFrame();
                if (audioFrame != null) {
                    try {
                        outputStream.write(audioFrame.array());
                        outputStream.flush();
                    } catch (IOException e) {
                        logger.error("tts for user {} failed", userId, e);
                    }
                }
            }

            @Override
            public void onComplete() {
                System.out.println("收到Complete");
            }

            @Override
            public void onError(Exception e) {
                System.out.println("收到错误: " + e.toString());
            }
        });
        //synthesizer.streamingCall(answer.getContent());
        if (answer.isDone()) {
            //synthesizer.streamingComplete();
//            FileInputStream file = new FileInputStream("D:\\projects\\ai-chat\\output.mp3");
//            IOUtils.copy(file, outputStream);
            Usage usage = answer.getUsage();
            if (usage == null) {
                usage = Usage.builder().user(user).build();
            }
            usage.setVoiceTokens(ttsTokens.get());
//            done(userId);
        }
        return false;
    }

    public void done(String userId) {
        OutputStream outputStream = streams.get(userId);
        if (outputStream != null) {
            this.notifyAll();
            streams.remove(userId);
        }
    }

    public boolean isListening(String userId) {
        return streams.containsKey(userId);
    }
}
