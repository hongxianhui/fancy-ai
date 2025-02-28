package com.fancy.aichat.client.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.ToolCallResultConverter;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.context.annotation.Description;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public interface ChatTool {

    static List<ToolCallback> generateToolCallbacks(Class<? extends ChatTool> clazz) {
        List<ToolCallback> toolCallbacks = new ArrayList<>(3);
        ReflectionUtils.doWithMethods(clazz, method -> {
            Description description = method.getAnnotation(Description.class);
            toolCallbacks.add(MethodToolCallback.builder()
                    .toolDefinition(ToolDefinition.builder(method)
                            .name(method.getName())
                            .description(description.value())
                            .build())
                    .toolCallResultConverter(new DirectCallResultConverter())
                    .toolMetadata(ToolMetadata.builder().returnDirect(true).build())
                    .toolMethod(method)
                    .build());
        }, method -> method.isAnnotationPresent(Description.class));
        return toolCallbacks;
    }

    class DirectCallResultConverter implements ToolCallResultConverter {

        @Override
        public String convert(Object result, Type returnType) {
            return (String) result;
        }
    }
}
