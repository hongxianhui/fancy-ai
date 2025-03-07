package cn.fancyai.chat.client.handler;

import cn.fancyai.chat.objects.Question;

/**
 * 100:SystemQuestionHandler
 * <p>
 * 410:PosterQuestionHandler
 * 420:GeneraImageQuestionHandler
 * 430:ImageVLQuestionHandler
 * <p>
 * 510:OllamaQuestionHandler
 * 520:QWen25QuestionHandler
 * 530:QWenPlusQuestionHandler
 * 540:QWenCoderPlusQuestionHandler
 * 550:DeepSeekR1QuestionHandler
 */
public interface QuestionHandler {

    boolean handle(Question question, HandlerContext context) throws Exception;

}
