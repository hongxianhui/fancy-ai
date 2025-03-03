package cn.fancyai.chat.client.handler;

import cn.fancyai.chat.objects.Answer;

/**
 * 10:TTSQuestionHandler
 * 20:SambertSpeechAnswerHandler
 */
public interface AnswerHandler {

    boolean handle(Answer answer, HandlerContext context) throws Exception;

}
