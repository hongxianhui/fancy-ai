package cn.fancyai.chat.client.handler;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.client.handler.text.DeepSeekR1QuestionHandlerAbstract;
import cn.fancyai.chat.client.handler.text.OllamaQuestionHandler;
import cn.fancyai.chat.client.handler.text.QWenPlusQuestionHandlerAbstract;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(100)
public class SystemQuestionHandler implements QuestionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SystemQuestionHandler.class);

    @Resource
    private UserManager userManager;

    @Override
    public boolean handle(Question question, HandlerContext context) throws Exception {
        String content = question.getContent();
        User user = question.getUser();
        if (content.equals("理解图片内容")) {
            logger.info("Handler: {}, Question: {}", getClass().getSimpleName(), ChatUtils.serialize(question));
            context.mute();
            user.getChatSession().sendMessage(getAnswer(user, ChatUtils.getText("image-vl.html")), context);
            return true;
        }
        if (content.startsWith("一键生成图片")) {
            logger.info("Handler: {}, Question: {}", getClass().getSimpleName(), ChatUtils.serialize(question));
            context.mute();
            user.getChatSession().sendMessage(getAnswer(user, ChatUtils.getText("images.html")), context);
            return true;
        }
        if (content.startsWith("选择聊天角色")) {
            logger.info("Handler: {}, Question: {}", getClass().getSimpleName(), ChatUtils.serialize(question));
            context.mute();
            user.getChatSession().sendMessage(getAnswer(user, ChatUtils.getText("roles.html")), context);
            return true;
        }
        if (content.startsWith("选择朗读音色")) {
            logger.info("Handler: {}, Question: {}", getClass().getSimpleName(), ChatUtils.serialize(question));
            context.mute();
            user.getChatSession().sendMessage(getAnswer(user, ChatUtils.getText("tones.html")), context);
            return true;
        }
        if (content.startsWith("关闭语音朗读")) {
            logger.info("Handler: {}, Question: {}", getClass().getSimpleName(), ChatUtils.serialize(question));
            question.getUser().getModel().setSpeech(null);
            user.getChatSession().sendMessage(getAnswer(user, "语音朗读功能已关闭。"), context);
            return true;
        }
        if (content.startsWith("切换音色")) {
            logger.info("Handler: {}, Question: {}", getClass().getSimpleName(), ChatUtils.serialize(question));
            switchTone(content, user);
            user.getChatSession().sendMessage(getAnswer(user, "音色已切换。"), context);
            return true;
        }
        if (content.startsWith("切换角色")) {
            logger.info("Handler: {}, Question: {}", getClass().getSimpleName(), ChatUtils.serialize(question));
            switchRole(content, user);
            context.mute();
            user.getChatSession().sendMessage(getAnswer(user, ChatUtils.getText(user.getModel().getChat().replaceAll(":", "_") + ".html")), context);
            return true;
        }
        if (content.startsWith("查询系统在线用户")) {
            logger.info("Handler: {}, Question: {}", getClass().getSimpleName(), ChatUtils.serialize(question));
            context.mute();
            List<User> users = userManager.getUsers();
            StringBuilder answer = new StringBuilder();
            answer.append("系统当前在线用户人数一共有").append(users.size()).append("人：\n\n");
            for (User onlineUser : users) {
                answer.append(onlineUser.getUserId()).append("\n");
            }
            user.getChatSession().sendMessage(getAnswer(user, answer.toString()), context);
            return true;
        }
        return false;
    }

    private Answer getAnswer(User user, String content) {
        return Answer.builder()
                .user(user)
                .type(Answer.TYPE_TOOL)
                .content(content)
                .done()
                .build();
    }

    private static void switchRole(String content, User user) {
        if (content.endsWith("小欧")) {
            user.getModel().setChat(OllamaQuestionHandler.MODEL_NAME);
        }
        if (content.endsWith("小千")) {
            user.getModel().setChat(QWenPlusQuestionHandlerAbstract.MODEL_NAME);
        }
        if (content.endsWith("小迪")) {
            user.getModel().setChat(DeepSeekR1QuestionHandlerAbstract.MODEL_NAME);
        }
    }

    private static void switchTone(String content, User user) {
        if (content.endsWith("站长")) {
            user.getModel().setSpeech("cosyvoice-v1");
        }
        if (content.endsWith("知楠")) {
            user.getModel().setSpeech("sambert-zhinan-v1");
        }
        if (content.endsWith("知琪")) {
            user.getModel().setSpeech("sambert-zhiqi-v1");
        }
        if (content.endsWith("知厨")) {
            user.getModel().setSpeech("sambert-zhichu-v1");
        }
        if (content.endsWith("知德")) {
            user.getModel().setSpeech("sambert-zhide-v1");
        }
        if (content.endsWith("知佳")) {
            user.getModel().setSpeech("sambert-zhijia-v1");
        }
        if (content.endsWith("知茹")) {
            user.getModel().setSpeech("sambert-zhiru-v1");
        }
        if (content.endsWith("知倩")) {
            user.getModel().setSpeech("sambert-zhiqian-v1");
        }
        if (content.endsWith("知祥")) {
            user.getModel().setSpeech("sambert-zhixiang-v1");
        }
        if (content.endsWith("知薇")) {
            user.getModel().setSpeech("sambert-zhiwei-v1");
        }
        if (content.endsWith("知浩")) {
            user.getModel().setSpeech("sambert-zhihao-v1");
        }
        if (content.endsWith("知婧")) {
            user.getModel().setSpeech("sambert-zhijing-v1");
        }
        if (content.endsWith("知茗")) {
            user.getModel().setSpeech("sambert-zhiming-v1");
        }
        if (content.endsWith("知墨")) {
            user.getModel().setSpeech("sambert-zhimo-v1");
        }
        if (content.endsWith("知娜")) {
            user.getModel().setSpeech("sambert-zhina-v1");
        }
        if (content.endsWith("知树")) {
            user.getModel().setSpeech("sambert-zhishu-v1");
        }
        if (content.endsWith("知莎")) {
            user.getModel().setSpeech("sambert-zhistella-v1");
        }
        if (content.endsWith("知婷")) {
            user.getModel().setSpeech("sambert-zhiting-v1");
        }
        if (content.endsWith("知笑")) {
            user.getModel().setSpeech("sambert-zhixiao-v1");
        }
        if (content.endsWith("知雅")) {
            user.getModel().setSpeech("sambert-zhiya-v1");
        }
        if (content.endsWith("知晔")) {
            user.getModel().setSpeech("sambert-zhiye-v1");
        }
        if (content.endsWith("知颖")) {
            user.getModel().setSpeech("sambert-zhiying-v1");
        }
        if (content.endsWith("知媛")) {
            user.getModel().setSpeech("sambert-zhiyuan-v1");
        }
        if (content.endsWith("知悦")) {
            user.getModel().setSpeech("sambert-zhiyue-v1");
        }
        if (content.endsWith("知柜")) {
            user.getModel().setSpeech("sambert-zhigui-v1");
        }
        if (content.endsWith("知硕")) {
            user.getModel().setSpeech("sambert-zhishuo-v1");
        }
        if (content.endsWith("知妙（多情感）")) {
            user.getModel().setSpeech("sambert-zhimiao-emo-v1");
        }
        if (content.endsWith("知猫")) {
            user.getModel().setSpeech("sambert-zhimao-v1");
        }
        if (content.endsWith("知伦")) {
            user.getModel().setSpeech("sambert-zhilun-v1");
        }
        if (content.endsWith("知飞")) {
            user.getModel().setSpeech("sambert-zhifei-v1");
        }
        if (content.endsWith("知达")) {
            user.getModel().setSpeech("sambert-zhida-v1");
        }
    }

}
