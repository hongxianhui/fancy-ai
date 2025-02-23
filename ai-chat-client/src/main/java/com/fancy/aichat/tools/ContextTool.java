package com.fancy.aichat.tools;

import org.fancy.aichat.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class ContextTool implements Tools {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private User[] users = new User[0];

    @Tool(description = "获取系统当前在线用户人数和用户标识")
    public String getOnlineUserCount() {
        logger.info("Tool call: getOnlineUserCount");
        StringBuilder answer = new StringBuilder();
        answer.append("系统当前在线用户人数一共").append(users.length).append("人，用户ID分别是：\n");
        for (User user : users) {
            answer.append(user.getUserId()).append("\n");
        }
        return answer.toString();
    }

    public void setUsers(User[] users) {
        this.users = users;
    }

}
