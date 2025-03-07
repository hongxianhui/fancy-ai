package cn.fancyai.chat.client.tools;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class GetOnlineUserInfoTool implements ChatTool, Supplier<String> {
    protected final static Logger logger = LoggerFactory.getLogger(GetOnlineUserInfoTool.class);

    @Override
    public String get() {
        logger.info("Tool called: getOnlineUserInfo");
        UserManager userManager = ServerApplication.applicationContext.getBean(UserManager.class, "userManager");
        List<User> users = userManager.getUsers();
        StringBuffer message = new StringBuffer("当前在线用户一共有" + users.size() + "人：");
        users.forEach(user -> {
            message.append(user.getUserId()).append("\n");
        });
        return message.substring(0, message.length() - 1);
    }
}
