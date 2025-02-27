package com.fancy.aichat.client.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddKnowledgeTool implements ChatTool {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String knowledge;

    public AddKnowledgeTool(String knowledge) {
        this.knowledge = knowledge;
    }

    public String getCurrentDateTime() {
        logger.info("Tool call: getCurrentDateTime");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "当前时间：" + now.format(formatter) + "。";
    }

}