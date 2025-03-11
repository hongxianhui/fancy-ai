package cn.fancyai.chat.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;

@Builder
@Getter
@Setter
public class ChatUsage extends UsageBase {
    private int promptTokens;
    private int completionTokens;
    private int speechTokens;
    private int imageAmount;
    private int imageTokens;
    private int videoDuration;
    private float fee;
    @JsonIgnore
    private User user;

    public String getCost() {
        calculateFee();
        if (fee == 0) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#0.0000");
        String result = df.format(fee) + "分";
        if (fee > 10) {
            fee = fee / 10;
            result = df.format(fee) + "角";
        }
        if (fee > 10) {
            fee = fee / 10;
            result = df.format(fee) + "元";
        }
        return result;
    }

    private void calculateFee() {
        if (this.fee > 0) {
            return;
        }
        //计算文字费用
        String chatModel = user.getModel().getChat();
        float fee = getQuestionTokenFee(chatModel, promptTokens);

        //计算语音朗读费用
        String speechModel = user.getModel().getSpeech();
        if (StringUtils.hasText(speechModel)) {
            fee += getSpeechFee(speechModel, speechTokens);
        }
        //计算工具费用
        String toolModel = user.getModel().getTool();
        if (StringUtils.hasText(toolModel)) {
            switch (toolModel) {
                case "wanx-poster-generation-v1":
                case "flux-dev":
                    break;
                case "wanx2.0-t2i-turbo":
                case "wanx2.1-t2i-turbo":
                case "wanx2.1-t2i-plus":
                    fee += getImageAnswerAmountFee(toolModel, imageAmount);
                    break;
                case "qwen-vl-max":
                case "qwen-vl-plus":
                    fee += getQuestionTokenFee(toolModel, promptTokens);
                    fee += getAnswerTokenFee(toolModel, completionTokens);
                    fee += getImageAnswerTokenFee(toolModel, imageTokens);
                    break;
                case "wanx2.1-t2v-turbo":
                case "wanx2.1-t2v-plus":
                    fee += getVideoFee(toolModel, videoDuration);
            }
        }
        this.fee = fee;
    }
}
