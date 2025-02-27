package com.fancy.aichat.client.tools.administration;

import com.fancy.aichat.ServerApplication;
import com.fancy.aichat.client.tools.ChatTool;
import com.fancy.aichat.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.ApplicationContext;

public class QueryMasterTool implements ChatTool {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Tool(description = "根据用户ID查询权限是否已激活", returnDirect = true)
    public String queryMaster(ToolContext context) {
        ApplicationContext applicationContext = ServerApplication.applicationContext;
        User user = (User) context.getContext().get("user");
        logger.info("Tool called queryMaster, user={}", user.getUserId());
        if (user.isAdmin()) {
            return "权限已激活。";
        }
        return "您还没有激活权限。";
    }

}
