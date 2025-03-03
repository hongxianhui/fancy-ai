package cn.fancyai.chat.objects;

import cn.fancyai.chat.client.handler.speech.CosySpeechAnswerHandler;
import cn.fancyai.chat.client.handler.text.DeepSeekR1QuestionHandlerAbstract;
import cn.fancyai.chat.client.handler.text.QWenPlusQuestionHandlerAbstract;
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
    private int speechTokens;
    private int imageAmount;
    private int imageTokens;
    private String cost;
    @JsonIgnore
    private User user;

    public String getCost() {
        DecimalFormat df = new DecimalFormat("#0.0000");
        double num = 0d;
        //计算文字费用
        String chatModel = user.getModel().getChat();
        switch (chatModel) {
            case DeepSeekR1QuestionHandlerAbstract.MODEL_NAME:
                num = 0.004d / 10 * promptTokens + 0.016d / 10 * completionTokens;
                break;
            case QWenPlusQuestionHandlerAbstract.MODEL_NAME:
                num = 0.0008d / 10 * promptTokens + 0.002d / 10 * completionTokens;
                break;
            case "qwen-vl-max":
                num = 0.003d / 10 * promptTokens + 0.002d / 10 * (completionTokens + imageTokens);
                break;
            case "qwen-vl-plus":
                num = 0.0015d / 10 * promptTokens + 0.00075d / 10 * (completionTokens + imageTokens);
                break;
        }
        //计算语音朗读费用
        String speechModel = user.getModel().getSpeech();
        if (StringUtils.hasText(speechModel)) {
            if (CosySpeechAnswerHandler.MODEL_NAME.equals(speechModel)) {
                num += 2d / 100 * speechTokens;
            } else {
                num += 1d / 100 * speechTokens;
            }
        }
        //计算图片费用
        String imageModel = user.getModel().getImage();
        if (StringUtils.hasText(imageModel)) {
            switch (imageModel) {
                case "wanx-poster-generation-v1":
                case "flux-dev":
                    return "限时免费";
                case "wanx2.0-t2i-turbo":
                    num += 4d * imageAmount;
                    break;
                case "wanx2.1-t2i-turbo":
                    num += 14d * imageAmount;
                    break;
                case "wanx2.1-t2i-plus":
                    num += 20d * imageAmount;
            }
        }
        String result = df.format(num) + "分";
        if (num > 10) {
            num = num / 10;
            result = df.format(num) + "角";
        }
        if (num > 10) {
            num = num / 10;
            result = df.format(num) + "元";
        }
        return result;
    }
}
