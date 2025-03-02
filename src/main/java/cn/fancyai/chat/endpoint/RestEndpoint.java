package cn.fancyai.chat.endpoint;

import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.client.handler.answer.TTSAnswerHandler;
import cn.fancyai.chat.objects.User;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@RestController
public class RestEndpoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private TTSAnswerHandler ttsAnswerHandler;
    @Resource
    private UserManager userManager;

    @GetMapping("/prompts")
    public List<String> prompts() {
        return List.of(
                "切换角色",
                "开启语音功能",
                "关闭语音功能",
                "查询在线用户信息"
        );
    }

    @GetMapping("/tts/{userId}")
    public ResponseEntity<StreamingResponseBody> tts(@PathVariable final String userId) {
        User user = userManager.getUser(userId);
        if (user == null || !Boolean.TRUE.equals(user.getMetadata().get(User.META_VOICE))) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().contentType(new MediaType("audio", "mp3"))
                .body(new StreamingResponseBody() {
                    @Override
                    public void writeTo(OutputStream outputStream) throws IOException {
                        if (ttsAnswerHandler.isListening(userId)) {
                            ttsAnswerHandler.done(userId);
                        }
                        ttsAnswerHandler.addVoiceListener(userId, outputStream);
                        try {
                            synchronized (outputStream) {
                                outputStream.wait(5 * 60 * 1000);
                            }
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }
                        logger.info("TTS completed for user {}", userId);
                    }
                });
    }
}
