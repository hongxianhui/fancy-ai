package cn.fancyai.chat.client.handler;

import cn.fancyai.chat.objects.Answer;

/**
 * 10:TTSQuestionHandler
 */
public interface AnswerHandler {

    boolean handle(Answer answer) throws Exception;

}
