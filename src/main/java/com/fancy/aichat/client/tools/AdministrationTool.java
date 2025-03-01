package com.fancy.aichat.client.tools;

import com.fancy.aichat.ServerApplication;
import com.fancy.aichat.manager.UserManager;
import com.fancy.aichat.objects.Question;
import com.fancy.aichat.objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Description;

import java.util.List;

public class AdministrationTool implements ChatTool {
    protected final static Logger logger = LoggerFactory.getLogger(AdministrationTool.class);

    @Description("查询系统在线用户信息")
    public static String getUserInfo(ToolContext context) {
        logger.info("Tool called: getUserInfo");
        UserManager userManager = ServerApplication.applicationContext.getBean("userManager", UserManager.class);
        List<User> users = userManager.getUsers();
        StringBuilder answer = new StringBuilder();
        answer.append("系统当前在线用户人数一共有").append(users.size()).append("人，他们是：\n");
        for (User user : users) {
            answer.append(user.getUserId()).append("\n");
        }
        return answer.toString();
    }

    @Description("开启语音功能")
    public static String enableVoice(ToolContext context) {
        logger.info("Tool called: enableVoice");
        Question question = (Question) context.getContext().get("question");
        if (question.getUser().getApiKey() != null) {
            return "站长囊中羞涩，匿名用户无法开启语音朗读功能。";
        }
        question.getUser().getMetadata().put(User.META_VOICE, true);
        return "语音朗读已开启。";
    }

    @Description("关闭语音功能")
    public static String disableVoice(ToolContext context) {
        logger.info("Tool called: disableVoice");
        Question question = (Question) context.getContext().get("question");
        question.getUser().getMetadata().put(User.META_VOICE, false);
        return "语音朗读已关闭。";
    }
}
