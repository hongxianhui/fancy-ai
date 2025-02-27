package com.fancy.aichat.endpoint;

import com.fancy.aichat.server.TTSServer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@RestController
public class RestEndpoint {
    @Resource
    private TTSServer ttsServer;

    @GetMapping("/prompts")
    public List<Map<String, String>> prompts() {
//        return Arrays.stream(ChatPrompt.values()).filter(ChatPrompt::isUser).map(chatPrompt -> {
//            Map<String, String> map = new HashMap<>();
//            map.put("id", chatPrompt.name());
//            map.put("placeHolder", chatPrompt.getPlaceHolder());
//            map.put("prompt", chatPrompt.getPrompt());
//            return map;
//        }).collect(Collectors.toList());
        return Collections.EMPTY_LIST;
    }

    @GetMapping("/tts/{userId}")
    public Callable<Void> tts(@PathVariable final String userId, HttpServletResponse response) {
        response.setContentType("audio/wav");
        return () -> {
            if (ttsServer.onAir(userId)) {
                ttsServer.done(userId);
            }
            ttsServer.stream(userId, response.getOutputStream());
            while (ttsServer.onAir(userId)) {
                Thread.sleep(500);
            }
            return null;
        };
    }
}
