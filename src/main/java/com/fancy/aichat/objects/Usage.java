package com.fancy.aichat.objects;

import com.fancy.aichat.client.handler.DeepSeekR1QuestionHandler;
import com.fancy.aichat.client.handler.QWenPlusQuestionHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

import java.text.DecimalFormat;

@Builder
@Getter
public class Usage {
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer voiceTokens;
    @JsonIgnore
    private Question question;

    public String getCost() {
        DecimalFormat df = new DecimalFormat("#0.0000");
        Double cost = 0d;
        switch (question.getUser().getModel()) {
            case DeepSeekR1QuestionHandler.MODEL_NAME:
                cost = 0.004d / 10 * promptTokens + 0.016d / 10 * completionTokens;
            case QWenPlusQuestionHandler.MODEL_NAME:
                cost = 0.0008d / 10 * promptTokens + 0.002d / 10 * completionTokens;
        }
        if (Boolean.TRUE.equals(question.getUser().getMetadata().get(User.META_VOICE))) {
            cost += 1d / 100 * voiceTokens;
        }
        return df.format(cost);
    }
}
