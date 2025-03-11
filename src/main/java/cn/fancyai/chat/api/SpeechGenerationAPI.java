package cn.fancyai.chat.api;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.objects.ChatUsage;
import cn.fancyai.chat.objects.UsageBase;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.tts.SpeechSynthesizer;
import org.springframework.stereotype.Component;

@Component
public class SpeechGenerationAPI {
    public byte[] generate(User user, String text, ChatUsage chatUsage) throws Exception {
        SpeechSynthesizer synthesizer = new SpeechSynthesizer();
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(user.getModel().getTool())
                .sampleRate(48000)
//                .enableWordTimestamp(true)
                .format(SpeechSynthesisAudioFormat.MP3)
//                .enablePhonemeTimestamp(true)
                .text(text)
                .build();
        chatUsage.setSpeechTokens(chatUsage.getSpeechTokens() + text.length());
        UsageBase calculator = new UsageBase();
        float answerFee = calculator.getSpeechFee(user.getModel().getTool(), text.length());
        chatUsage.setFee(chatUsage.getFee() + answerFee);
        return synthesizer.call(param).array();
    }
}
