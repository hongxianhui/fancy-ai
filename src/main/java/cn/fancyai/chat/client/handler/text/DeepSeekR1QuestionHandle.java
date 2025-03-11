package cn.fancyai.chat.client.handler.text;

import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(550)
public class DeepSeekR1QuestionHandle extends AbstractStreamingTextQuestionHandler {
    @Override
    protected String getModelName() {
        return "deepseek-r1";
    }

    @Override
    protected Answer getAnswerOnStream(ChatResponse chatResponse, Question question, HandlerContext context) {
        Generation result = chatResponse.getResult();
        if (result == null) {
            return null;
        }
        String finishReason = result.getMetadata().getFinishReason();
        String reasoning = (String) result.getOutput().getMetadata().get("reasoningContent");
        String content = result.getOutput().getText();
        Answer previousAnswer = (Answer) context.get("previousAnswer");
        Answer.Builder builder = Answer.builder(question.getUser()).type(Answer.TYPE_THINK).content(reasoning);
        if (!content.isBlank()) {
            if (previousAnswer != null && Answer.TYPE_THINK.equals(previousAnswer.getType())) {
                content = "\n\n" + content;
            }
            builder.type(Answer.TYPE_ANSWER).content(content);
        }
        if (finishReason != null && !"null".equalsIgnoreCase(finishReason)) {
            builder.done();
        }
        return builder.build();
    }
}
