package cn.fancyai.voice;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.audio.ttsv2.enrollment.Voice;
import com.alibaba.dashscope.audio.ttsv2.enrollment.VoiceEnrollmentService;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import static java.lang.System.exit;

public class VoiceCreator {
    public static String apiKey = "sk-d99cc195827a4ae395d70863c035313a";
    private static String fileUrl = "http://47.104.251.206/record.wav";  // 请按实际情况进行替换
    private static String prefix = "fancy";
    private static String targetModel = "cosyvoice-v1";

    public static void main(String[] args)
            throws NoApiKeyException, InputRequiredException {
        // 复刻声音
        VoiceEnrollmentService service = new VoiceEnrollmentService(apiKey);
        Voice myVoice = service.createVoice(targetModel, prefix, fileUrl);
        System.out.println("RequestId: " + service.getLastRequestId());
        System.out.println("your voice id is " + myVoice.getVoiceId());
        // 使用复刻的声音来合成文本为语音
        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .apiKey(apiKey)
                .model(targetModel)
                .voice(myVoice.getVoiceId())
                .build();
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);
        ByteBuffer audio = synthesizer.call("今天天气怎么样？");
        // 保存合成的语音到文件
        System.out.println("TTS RequestId: " + synthesizer.getLastRequestId());
        File file = new File("output.mp3");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(audio.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        exit(0);
    }
}