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
public class ChatUsage {
    private long promptTokens;
    private long completionTokens;
    private long speechTokens;
    private long imageAmount;
    private long imageTokens;
    private long videoDuration;
    private String cost;
    @JsonIgnore
    private User user;

    public String getCost() {
        DecimalFormat df = new DecimalFormat("#0.0000");
        //计算文字费用
        String chatModel = user.getModel().getChat();
        float fee = switch (chatModel) {
            case "deepseek-r1" -> 0.004f * 100 / 1000 * promptTokens + 0.016f * 100 / 1000 * completionTokens;
            case "qwen-plus" -> 0.0008f * 100 / 1000 * promptTokens + 0.002f * 100 / 1000 * completionTokens;
            case "qwen-coder-plus" -> 0.0035f * 100 / 1000 * promptTokens + 0.007f * 100 / 1000 * completionTokens;
            default -> 0;
        };
        //计算语音朗读费用
        String speechModel = user.getModel().getSpeech();
        if (StringUtils.hasText(speechModel)) {
            if (speechModel.equals("cosyvoice-v1")) {
                fee += 2f * 100 / 10000 * speechTokens;
            } else {
                fee += 1f * 100 / 10000 * speechTokens;
            }
        }
        //计算工具费用
        String toolModel = user.getModel().getTool();
        if (StringUtils.hasText(toolModel)) {
            switch (toolModel) {
                case "wanx-poster-generation-v1":
                case "flux-dev":
                    break;
                case "wanx2.0-t2i-turbo":
                    fee += 4f * imageAmount;
                    break;
                case "wanx2.1-t2i-turbo":
                    fee += 14f * imageAmount;
                    break;
                case "wanx2.1-t2i-plus":
                    fee += 20f * imageAmount;
                case "qwen-vl-max":
                    fee += 0.003f * 100 / 1000 * promptTokens + 0.002f * 100 / 1000 * (completionTokens + imageTokens);
                    break;
                case "qwen-vl-plus":
                    fee += 0.0015f * 100 / 1000 * promptTokens + 0.00075f * 100 / 1000 * (completionTokens + imageTokens);
                    break;
                case "wanx2.1-t2v-turbo":
                    fee += 24f * videoDuration;
                    break;
                case "wanx2.1-t2v-plus":
                    fee += 70f * videoDuration;
                    break;
            }
        }
        if (fee == 0) {
            return null;
        }
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
}
