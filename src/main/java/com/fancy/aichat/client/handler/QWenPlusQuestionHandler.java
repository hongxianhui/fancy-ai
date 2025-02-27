package com.fancy.aichat.client.handler;

import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.alibaba.dashscope.tools.ToolFunction;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fancy.aichat.client.tools.GetUserInfoTool;
import com.fancy.aichat.common.Answer;
import com.fancy.aichat.common.Question;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Order(2)
public class QWenPlusQuestionHandler extends APIQuestionHandler {
    public static final String MODEL_NAME = "qwen-plus";

    @Override
    protected String getModelName() {
        return MODEL_NAME;
    }

    public QWenPlusQuestionHandler(ChatClient.Builder modelBuilder) {

    }

    @Override
    protected List<Message> getSystemMessage(Question question) {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder().role(Role.SYSTEM.getValue())
                .content(ResourceUtils.getText("classpath:prompt/api-identity.txt"))
                .build());
        messages.add(Message.builder().role(Role.SYSTEM.getValue())
                .content(ResourceUtils.getText("classpath:prompt/api-functioncall.txt"))
                .build());
        messages.addAll(super.getSystemMessage(question));
        return messages;
    }

    @Override
    protected void customizeGenerationParam(Question question, GenerationParam.GenerationParamBuilder<?, ?>
            builder) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        SchemaGeneratorConfig config = configBuilder.with(Option.EXTRA_OPEN_API_FORMAT_VALUES).without(Option.FLATTENED_ENUMS_FROM_TOSTRING).build();
        SchemaGenerator generator = new SchemaGenerator(config);
        ObjectNode jsonSchemaGetUserInfoTool = generator.generateSchema(GetUserInfoTool.class);
        ObjectNode jsonSchemaActiveApiKeyTool = generator.generateSchema(GetUserInfoTool.class);
        FunctionDefinition fdContext = FunctionDefinition.builder().name("getUserInfo").description("这个函数用来查询系统在线用户人数和用户信息")
                .parameters(JsonUtils.parseString(jsonSchemaGetUserInfoTool.toString()).getAsJsonObject()).build();
        FunctionDefinition fdTime = FunctionDefinition.builder().name("activeApiKey").description("这个函数用来录入APIKEY，录入成功后，小凡就可以使用联网功能了")
                .parameters(JsonUtils.parseString(jsonSchemaActiveApiKeyTool.toString()).getAsJsonObject()).build();
        builder.enableSearch(true).tools(Arrays.asList(
                ToolFunction.builder().function(fdContext).build(),
                ToolFunction.builder().function(fdTime).build()));
    }

    @Override
    protected Answer onStreamToken(GenerationResult token, Question question) {
        GenerationOutput.Choice choice = token.getOutput().getChoices().get(0);
        String content = choice.getMessage().getContent();
        Answer.Builder builder = Answer.builder().user(question.getUser()).type(Answer.TYPE_ANSWER).content(content);
        if ("stop".equals(choice.getFinishReason())) {
            builder.done();
            logger.info("Answer complete.");
        }
        return builder.build();
    }

}
