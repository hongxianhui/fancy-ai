package cn.fancyai.chat.client.tools;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
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
        question.getUser().getMetadata().put(User.META_VOICE, true);
        return "语音朗读功能已开启。";
    }

    @Description("关闭语音功能")
    public static String disableVoice(ToolContext context) {
        logger.info("Tool called: disableVoice");
        Question question = (Question) context.getContext().get("question");
        question.getUser().getMetadata().put(User.META_VOICE, false);
        return "语音朗读功能已关闭。";
    }
}
