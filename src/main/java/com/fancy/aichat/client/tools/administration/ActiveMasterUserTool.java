package com.fancy.aichat.client.tools.administration;

import com.fancy.aichat.client.tools.ChatTool;
import com.fancy.aichat.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class ActiveMasterUserTool implements ChatTool {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Tool(description = "该函数用来激活权限", returnDirect = true)
    public String activeMasterUser(@ToolParam(description = "口令") String password, ToolContext context) {
        User user = (User) context.getContext().get("user");
        String masterPassword = (String) context.getContext().get("masterPassword");
        logger.info("Tool called activeMasterUser, userId={}, password={}", user.getUserId(), password);
        if (!masterPassword.equals(password)) {
            return "口令不正确。";
        }
        user.setAdmin(true);
        return "站长权限已激活。";
    }

}
