package cn.fancyai.chat.client.handler.exception;

import cn.fancyai.chat.objects.User;

public interface ChatExceptionHandler {

    void handle(User user, Throwable e);

    boolean accept(Throwable e);
}
