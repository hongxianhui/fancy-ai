package com.fancy.aichat.client.tools;

import com.fancy.aichat.ServerApplication;
import com.fancy.aichat.manager.UserManager;
import com.fancy.aichat.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetUserInfoTool implements ChatTool {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public String getUserInfo() {
        logger.info("Tool call: getUserInfo");
        UserManager userManager = ServerApplication.applicationContext.getBean("userManager", UserManager.class);
        List<User> users = userManager.getUsers();
        StringBuilder answer = new StringBuilder();
        answer.append("系统当前在线用户人数一共有").append(users.size()).append("人，他们分别是：\n");
        for (User user : users) {
            answer.append(user.getUserId()).append("\n");
        }
        return answer.toString();
    }

}
