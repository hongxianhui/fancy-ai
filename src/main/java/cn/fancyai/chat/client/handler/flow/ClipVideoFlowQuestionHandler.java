package cn.fancyai.chat.client.handler.flow;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.client.handler.exception.ChatExceptionConsumer;
import cn.fancyai.chat.client.worker.flow.GenerateClipVideoFlowWorker;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component()
@Scope("prototype")
@Order(210)
public class ClipVideoFlowQuestionHandler implements QuestionHandler {
    protected final Logger logger = LoggerFactory.getLogger(ClipVideoFlowQuestionHandler.class);

    public static final String VIDEO_FILE = "clipShow-" + System.currentTimeMillis() + ".mp4";
    private static GenerateClipVideoFlowWorker worker;

    @Override
    public boolean handle(Question question, HandlerContext context) throws Exception {
        String content = question.getContent();
        if (!content.startsWith("生成幻灯片：")) {
            return false;
        }
        User user = question.getUser();
        logger.info("Handle question: {}::{}", getClass().getSimpleName(), user.getModel().getTool());
        ChatUtils.sendMessage(user, "第一步：生成短视频文案\n");
        worker = new GenerateClipVideoFlowWorker(user, content.substring(6), getFileFromTempFolder(VIDEO_FILE))
                .generateVideoPlan()
                .formatVideoPlan();
        ChatUtils.sendMessage(user, "\n\n第二步：生成幻灯片插图\n");
        worker.generateImages(clipFrame -> {
            try {
                String text = "场景#" + clipFrame.getNo() + "  ";
                ChatUtils.sendMessage(user, text, Answer.TYPE_FLOW);
            } catch (IOException e) {
                new ChatExceptionConsumer(user).accept(e);
            }
        });
        ChatUtils.sendMessage(user, "\n\n第三步：生成配音\n");
        worker.generateSpeech(clipFrame -> {
            try {
                String text = "场景#" + clipFrame.getNo() + "  ";
                ChatUtils.sendMessage(user, text, Answer.TYPE_FLOW);
            } catch (IOException e) {
                new ChatExceptionConsumer(user).accept(e);
            }
        });
        ChatUtils.sendMessage(user, "\n\n第四步：渲染幻灯片字幕\n");
        worker.generateSubtitles(clipFrame -> {
            try {
                String text = "场景#" + clipFrame.getNo() + "  ";
                ChatUtils.sendMessage(user, text, Answer.TYPE_FLOW);
            } catch (IOException e) {
                new ChatExceptionConsumer(user).accept(e);
            }
        });
        ChatUtils.sendMessage(user, "\n\n第五步：渲染视频流\n");
        worker.generateVideo(clipFrame -> {
            try {
                String text = "场景#" + clipFrame.getNo() + "  ";
                ChatUtils.sendMessage(user, text, Answer.TYPE_FLOW);
                if (clipFrame.getNo() >= 6) {
                    ChatUtils.sendMessage(user, "\n\n第六步：渲染音频流\n");
                }
            } catch (IOException e) {
                new ChatExceptionConsumer(user).accept(e);
            }
        }, clipFrame -> {
            String text = "场景#" + clipFrame.getNo() + "  ";
            ChatUtils.sendMessageSilent(user, text, Answer.TYPE_FLOW);
        });
        ChatUtils.sendMessage(user, "\n\n第七步：导出文件\n");
        Thread.sleep(1000);
        ChatUtils.sendMessage(user, "完成", Answer.TYPE_FLOW);
        ChatUtils.sendMessage(user, "", Answer.TYPE_NEW);
        Answer answer = Answer.builder(user)
                .content(ChatUtils.serialize(Map.of("videoUrl", "/download?fileName=flow/" + VIDEO_FILE)))
                .usage(worker.getChatUsage())
                .type(Answer.TYPE_VIDEO)
                .done()
                .build();
        user.getChatSession().sendMessage(new TextMessage(ChatUtils.serialize(answer)));
        return true;
    }

    private File getFileFromTempFolder(String fileName) {
        String tempFolder = ServerApplication.applicationContext.getEnvironment().getProperty("ai.tempfile.folder");
        return new File(tempFolder + File.separatorChar + "flow" + File.separatorChar + fileName);
    }
}
