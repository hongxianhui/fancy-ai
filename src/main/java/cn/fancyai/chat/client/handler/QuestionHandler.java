package cn.fancyai.chat.client.handler;

import cn.fancyai.chat.client.handler.answer.AnswerCallback;
import cn.fancyai.chat.objects.Question;

/**
 * 10:SystemQuestionHandler
 * 20:OllamaQuestionHandler
 * 30:QWenPlusQuestionHandler
 * 40:DeepSeekR1QuestionHandler
 * 50:ImageQuestionHandler
 */
public interface QuestionHandler {

    boolean handle(Question question, AnswerCallback callback) throws Exception;

}
