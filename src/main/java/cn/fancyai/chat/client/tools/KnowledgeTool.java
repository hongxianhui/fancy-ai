package cn.fancyai.chat.client.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class KnowledgeTool implements ChatTool {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String knowledge;

    public KnowledgeTool(String knowledge) {
        this.knowledge = knowledge;
    }

    public String getCurrentDateTime() {
        logger.info("Tool call: getCurrentDateTime");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "当前时间：" + now.format(formatter) + "。";
    }

}