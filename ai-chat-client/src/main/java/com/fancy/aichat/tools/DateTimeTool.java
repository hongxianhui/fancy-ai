package com.fancy.aichat.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeTool implements Tools {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Tool(description = "按时区获取当前日期和时间")
    public String getCurrentDateTime() {
        logger.info("Tool call: getCurrentDateTime");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

}