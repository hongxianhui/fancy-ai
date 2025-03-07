package cn.fancyai.chat.client.handler.speech;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class CosySpeechAnswerHandler extends AbstractSpeechAnswerHandler {
    private static final Logger logger = LoggerFactory.getLogger(CosySpeechAnswerHandler.class);

    @Override
    protected String getModelName(Answer answer) {
        return "cosyvoice-v1";
    }

    @Override
    protected byte[] speech(Answer answer, String text) throws NoApiKeyException {
        User user = answer.getUser();
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(SpeechSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(getModelName(answer))
                .voice("cosyvoice-fancy-81c16fbbd75a43eaa50a4d00d5daa731")
                .build(), null);
        return synthesizer.call(text).array();
    }

}
