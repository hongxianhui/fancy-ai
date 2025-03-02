package cn.fancyai.chat.endpoint;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.AnswerHandler;
import cn.fancyai.chat.client.handler.answer.AnswerCallback;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Usage;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.common.ResultCallback;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class TTSStreamingResponseBody implements StreamingResponseBody, AnswerHandler {
    private final Logger logger = LoggerFactory.getLogger(TTSStreamingResponseBody.class);

    private OutputStream outputStream;
    private int ttsTokens;

    @Override
    public boolean handle(Answer answer) throws Exception {
        FileInputStream file = new FileInputStream("D:\\projects\\ai-chat\\output.mp3");
        IOUtils.copy(file, outputStream);
//        AnswerCallback.removeHandler(this);
//        User user = answer.getUser();
//        String userId = user.getUserId();
//        ttsTokens += answer.getContent().length();
//        SpeechSynthesizer synthesizer = new SpeechSynthesizer(SpeechSynthesisParam.builder()
//                .apiKey(ChatUtils.getApiKey(user))
//                .model("cosyvoice-v1")
//                .voice("cosyvoice-fancy-81c16fbbd75a43eaa50a4d00d5daa731")
//                .build(), new ResultCallback<>() {
//            @Override
//            public void onEvent(SpeechSynthesisResult result) {
//                ByteBuffer audioFrame = result.getAudioFrame();
//                if (audioFrame != null) {
//                    try {
//                        outputStream.write(audioFrame.array());
//                        outputStream.flush();
//                    } catch (IOException e) {
//                        logger.error("tts for user {} failed", userId, e);
//                    }
//                }
//            }
//
//            @Override
//            public void onComplete() {
//                System.out.println("收到Complete");
//            }
//
//            @Override
//            public void onError(Exception e) {
//                System.out.println("收到错误: " + e.toString());
//            }
//        });
//        //synthesizer.streamingCall(answer.getContent());
//        if (answer.isDone()) {
////            synthesizer.streamingComplete();
//            FileInputStream file = new FileInputStream("D:\\projects\\ai-chat\\output.mp3");
//            IOUtils.copy(file, outputStream);
//            Usage usage = answer.getUsage();
//            if (usage == null) {
//                usage = Usage.builder().user(user).build();
//            }
//            usage.setVoiceTokens(ttsTokens);
            notify();
//        }
        return false;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
//        AnswerCallback.addHandler(this);
        synchronized (this) {
            try {
                this.wait(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
