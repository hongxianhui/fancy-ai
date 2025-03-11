package cn.fancyai.chat.objects;

public class UsageBase {

    public float getQuestionTokenFee(String model, int tokens) {
        return switch (model) {
            case "deepseek-r1" -> 0.004f * 100 / 1000 * tokens;
            case "qwen-plus" -> 0.0008f * 100 / 1000 * tokens;
            case "qwen-coder-plus" -> 0.0035f * 100 / 1000 * tokens;
            case "qwen-vl-max" -> 0.003f * 100 / 1000 * tokens;
            case "qwen-vl-plus" -> 0.015f * 100 / 1000 * tokens;
            default -> 0;
        };
    }

    public float getAnswerTokenFee(String model, int tokens) {
        return switch (model) {
            case "deepseek-r1" -> 0.016f * 100 / 1000 * tokens;
            case "qwen-plus" -> 0.002f * 100 / 1000 * tokens;
            case "qwen-coder-plus" -> 0.007f * 100 / 1000 * tokens;
            case "qwen-vl-max" -> 0.002f * 100 / 1000 * tokens;
            case "qwen-vl-plus" -> 0.00075f * 100 / 1000 * tokens;
            default -> 0;
        };
    }

    public float getImageAnswerAmountFee(String model, int images) {
        return switch (model) {
            case "wanx2.0-t2i-turbo" -> 4f * images;
            case "wanx2.1-t2i-turbo" -> 14f * images;
            case "wanx2.1-t2i-plus" -> 20f * images;
            default -> 0;
        };
    }

    public float getImageAnswerTokenFee(String model, int tokens) {
        return switch (model) {
            case "qwen-vl-max" -> 0.002f * 100 / 1000 * tokens;
            case "qwen-vl-plus" -> 0.00075f * 100 / 1000 * tokens;
            default -> 0;
        };
    }

    public float getSpeechFee(String model, int tokens) {
        return switch (model) {
            case "cosyvoice-v1" -> 2f * 100 / 10000 * tokens;
            default -> 1f * 100 / 10000 * tokens;
        };
    }

    public float getVideoFee(String model, int seconds) {
        return switch (model) {
            case "wanx2.1-t2v-turbo" -> 24f * seconds;
            case "wanx2.1-t2v-plus" -> 70f * seconds;
            default -> 0;
        };
    }
}
