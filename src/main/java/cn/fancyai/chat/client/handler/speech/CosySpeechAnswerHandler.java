package cn.fancyai.chat.client.handler.speech;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(10)
public class CosySpeechAnswerHandler extends AbstractSpeechAnswerHandler {
    public static final String MODEL_NAME = "cosyvoice-v1";
    private static final Logger logger = LoggerFactory.getLogger(CosySpeechAnswerHandler.class);

    @Value("${spring.ai.dash-scope.audio.options.voice}")
    private String voice;

    @Override
    protected byte[] speech(User user, String text) throws NoApiKeyException {
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(SpeechSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(MODEL_NAME)
                .voice(voice)
                .build(), null);
        return synthesizer.call(text).array();
    }

    @Override
    public boolean handle(Answer answer, HandlerContext context) throws NoApiKeyException, IOException {
        User user = answer.getUser();
        if (!MODEL_NAME.equals(user.getModel().getSpeech())) {
            synchronized (user) {
                user.notifyAll();
            }
            return false;
        }
        return super.handle(answer, context);
    }

}
