package com.fancy.aichat.server;

import org.fancy.aichat.common.ChatPrompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class RestEndpoint {

    @GetMapping("/prompts")
    public List<Map<String, String>> prompts() {
        return Arrays.stream(ChatPrompt.values()).filter(ChatPrompt::isUser).map(chatPrompt -> {
            Map<String, String> map = new HashMap<>();
            map.put("key", chatPrompt.getKey());
            map.put("prompt", chatPrompt.getPrompt());
            return map;
        }).collect(Collectors.toList());
    }
}
