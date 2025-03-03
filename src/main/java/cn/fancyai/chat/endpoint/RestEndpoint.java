package cn.fancyai.chat.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RestEndpoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/prompts")
    public List<String> prompts() {
        return List.of(
                "选择聊天角色",
                "选择朗读音色",
                "关闭语音朗读",
                "一键生成图片",
                "理解图片内容",
                "查询在线用户"
        );
    }
}
