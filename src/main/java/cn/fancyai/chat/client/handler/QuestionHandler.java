package cn.fancyai.chat.client.handler;

import cn.fancyai.chat.objects.Question;

/**
 * 100:SystemQuestionHandler
 * 490:ImageVLQuestionHandler
 * 500:PosterQuestionHandler
 * 510:ImageQuestionHandler
 * 520:OllamaQuestionHandler
 * 530:QWenPlusQuestionHandler
 * 540:DeepSeekR1QuestionHandler
 */
public interface QuestionHandler {

    boolean handle(Question question, HandlerContext context) throws Exception;

}
