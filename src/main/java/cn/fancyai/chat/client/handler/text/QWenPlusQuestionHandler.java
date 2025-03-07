package cn.fancyai.chat.client.handler.text;

import cn.fancyai.chat.objects.Question;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(530)
public class QWenPlusQuestionHandler extends AbstractStreamingTextQuestionHandler {
    @Override
    protected String getModelName() {
        return "qwen-plus";
    }

    @Override
    protected void customizeChatOperations(Question question, DashScopeChatOptions.DashscopeChatOptionsBuilder builder) {
        builder.withEnableSearch(true);
    }

}
