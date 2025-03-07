package cn.fancyai.chat.client.handler.text;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(540)
public class QWenCoderPlusQuestionHandler extends QWenPlusQuestionHandler {
    @Override
    protected String getModelName() {
        return "qwen-coder-plus";
    }
}
