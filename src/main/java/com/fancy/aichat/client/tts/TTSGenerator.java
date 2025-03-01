package com.fancy.aichat.client.tts;

import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fancy.aichat.objects.User;
import com.fancy.aichat.objects.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class TTSGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TTSGenerator.class);

    @Value("${spring.ai.dash-scope.audio.options.model}")
    private String model;

    private final SpeechSynthesizer synthesizer = new SpeechSynthesizer();
    private final Map<String, OutputStream> streams = new HashMap<>();

    public void stream(String userId, OutputStream out) {
        streams.put(userId, out);
    }

    public void transform(User user, String content) throws NoApiKeyException {
        String userId = user.getUserId();
        OutputStream outputStream = streams.get(userId);
        if (outputStream == null) {
            logger.warn("stream for user {} not found", userId);
            return;
        }
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .apiKey(Utils.getApiKey(user))
                .model(model)
                .sampleRate(44100)
                .text(content)
                .build();
        long time = System.currentTimeMillis();
        byte[] audio = synthesizer.call(param).array();
        logger.info("tts convert content length {} cost {}.", content.length(), System.currentTimeMillis() - time);
        try {
            outputStream.write(audio);
            outputStream.flush();
        } catch (IOException e) {
            logger.error("tts for user {} failed", userId, e);
        }
    }

    public void done(String userId) {
        OutputStream outputStream = streams.get(userId);
        if (outputStream == null) {
            return;
        }
        streams.remove(userId);
    }

    public boolean onAir(String userId) {
        return streams.containsKey(userId);
    }
}
