package com.fancy.aichat.server;

import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class TTSServer {
    private static final Logger logger = LoggerFactory.getLogger(TTSServer.class);

    @Value("sk-d99cc195827a4ae395d70863c035313a")
    private String apiKey;
    private final SpeechSynthesizer synthesizer = new SpeechSynthesizer();

    private final Map<String, OutputStream> streams = new HashMap<>();

    public void stream(String userId, OutputStream out) {
        streams.put(userId, out);
    }

    public void transform(String userId, String content) {
        OutputStream outputStream = streams.get(userId);
        if (outputStream == null) {
            logger.warn("stream for user {} not found", userId);
            return;
        }
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .apiKey(apiKey)
                .model("sambert-zhixiang-v1")
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

    public void done(String userId) throws IOException {
        OutputStream outputStream = streams.get(userId);
        if (outputStream == null) {
            return;
        }
        outputStream.close();
        streams.remove(userId);
    }

    public boolean onAir(String userId) {
        return streams.containsKey(userId);
    }
}
