package com.fancy.aichat.endpoint;

import com.fancy.aichat.manager.UserManager;
import com.fancy.aichat.objects.User;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
public class RestEndpoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private TTSEndpoint ttsEndpoint;
    @Resource
    private UserManager userManager;
    @Resource
    private ThreadPoolTaskExecutor chatThreadPoolTaskExecutor;

    @GetMapping("/prompts")
    public List<String> prompts() {
        return List.of(
                "激活权限，口令: ",
                "切换小欧角色",
                "切换小千角色",
                "切换小迪角色",
                "开启语音功能",
                "关闭语音功能",
                "查一下权限是否已激活",
                "查一下在线用户信息"
        );
    }

    @GetMapping("/tts/{userId}")
    public ResponseEntity<StreamingResponseBody> tts(@PathVariable final String userId) {
        User user = userManager.getUser(userId);
        if (user == null || !user.isAdmin() || !Boolean.TRUE.equals(user.getMetadata().get(User.META_VOICE))) {
            return null;
        }
        return ResponseEntity.ok().contentType(new MediaType("audio", "wav"))
                .body(outputStream -> {
                    if (ttsEndpoint.onAir(userId)) {
                        ttsEndpoint.done(userId);
                    }
                    ttsEndpoint.stream(userId, outputStream);
                    while (ttsEndpoint.onAir(userId)) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    logger.info("TTS completed for user {}", userId);
                });
    }
}
