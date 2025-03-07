package cn.fancyai.chat.client.handler.exception;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.endpoint.ChatEndpoint;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class ChatExceptionConsumer implements Consumer<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(ChatEndpoint.class);

    private final User user;

    public ChatExceptionConsumer(User user) {
        this.user = user;
    }

    @Override
    public void accept(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
        Map<String, ChatExceptionHandler> handlers = ServerApplication.applicationContext.getBeansOfType(ChatExceptionHandler.class);
        handlers.values().stream().filter(chatExceptionHandler -> chatExceptionHandler.accept(throwable))
                .findFirst()
                .orElseGet(() -> new ChatExceptionHandler() {

                    @Override
                    public void handle(User user, Throwable e) {
                        logger.error("Uncaught error occurred.", e);
                        try {
                            Answer answer = Answer.builder(user).type(Answer.TYPE_TOOL).content("发生未知错误，请稍后重试。").done().build();
                            String answerJson = ChatUtils.serialize(answer);
                            logger.info("Answer: {}", answerJson);
                            logger.info("Cost: {}", ChatUtils.serialize(answer.getUsage()));
                            user.getChatSession().sendMessage(new TextMessage(answerJson));
                        } catch (IOException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }

                    @Override
                    public boolean accept(Throwable e) {
                        return true;
                    }
                })
                .handle(user, throwable);
    }

}
