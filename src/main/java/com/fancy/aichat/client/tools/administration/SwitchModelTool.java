package com.fancy.aichat.client.tools.administration;

import com.fancy.aichat.client.handler.DeepSeekR1QuestionHandler;
import com.fancy.aichat.client.handler.QWenPlusQuestionHandler;
import com.fancy.aichat.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class SwitchModelTool {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Tool(description = "该函数用来切换模型", returnDirect = true)
    public String switchModel(@ToolParam(description = "模型名称") String model, ToolContext context) {
        User user = (User) context.getContext().get("user");
        logger.info("Tool called switchModel, userId={}, model={}", user.getUserId(), model);
        if (!user.isAdmin()) {
            return "您没有站长权限，无法切换模型。";
        }
        String _model = model.toLowerCase();
        if (_model.contains("千问")) {
            user.setModel(QWenPlusQuestionHandler.MODEL_NAME);
            return "模型已切换至：" + QWenPlusQuestionHandler.MODEL_NAME;
        }
        if (_model.contains("深度求索") || _model.contains("deepseek")) {
            user.setModel(DeepSeekR1QuestionHandler.MODEL_NAME);
            return "模型已切换至：" + DeepSeekR1QuestionHandler.MODEL_NAME;
        }
        return "模型不存在：" + model;
    }
}
