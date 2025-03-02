package cn.fancyai.chat.objects;

import cn.fancyai.chat.client.handler.question.DeepSeekR1QuestionHandlerAbstract;
import cn.fancyai.chat.client.handler.question.ImageQuestionHandler;
import cn.fancyai.chat.client.handler.question.QWenPlusQuestionHandlerAbstract;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;

@Builder
@Getter
@Setter
public class Usage {
    private int promptTokens;
    private int completionTokens;
    private int voiceTokens;
    private int imageAmount;
    private String cost;
    @JsonIgnore
    private User user;

    public String getCost() {
        if (StringUtils.hasText(cost)) {
            return cost;
        }
        DecimalFormat df = new DecimalFormat("#0.0000");
        String result = null;
        switch (user.getModel()) {
            case DeepSeekR1QuestionHandlerAbstract.MODEL_NAME:
                double cost = 0.004d / 10 * promptTokens + 0.016d / 10 * completionTokens;
                if (Boolean.TRUE.equals(user.getMetadata().get(User.META_VOICE))) {
                    cost += 1d / 100 * voiceTokens;
                }
                result = df.format(cost) + "分";
                break;
            case QWenPlusQuestionHandlerAbstract.MODEL_NAME:
                cost = 0.0008d / 10 * promptTokens + 0.002d / 10 * completionTokens;
                if (Boolean.TRUE.equals(user.getMetadata().get(User.META_VOICE))) {
                    cost += 1d / 100 * voiceTokens;
                }
                result = df.format(cost) + "分";
                break;
            case ImageQuestionHandler.MODEL_NAME:
                result = df.format(0.14d * imageAmount) + "元";
                break;
        }
        return result;
    }
}
