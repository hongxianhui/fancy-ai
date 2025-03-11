package cn.fancyai.chat.client.handler.speech;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class SambertSpeechAnswerHandler extends AbstractSpeechAnswerHandler {
    private static final Logger logger = LoggerFactory.getLogger(SambertSpeechAnswerHandler.class);

    @Override
    protected String getModelName(Answer answer) {
        return answer.getUser().getModel().getSpeech();
    }

    @Override
    protected byte[] speech(Answer answer, String text) throws NoApiKeyException {
        User user = answer.getUser();
        SpeechSynthesizer synthesizer = new SpeechSynthesizer();
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(getModelName(answer))
                .format(SpeechSynthesisAudioFormat.WAV)
                .sampleRate(48000)
                .text(text)
                .build();
        return synthesizer.call(param).array();
    }

}
