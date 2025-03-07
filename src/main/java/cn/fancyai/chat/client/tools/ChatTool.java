package cn.fancyai.chat.client.tools;

import org.springframework.ai.model.function.FunctionCallback;

import java.util.ArrayList;
import java.util.List;

public interface ChatTool {

    static List<FunctionCallback> generateFunctionCallbacks() {
        List<FunctionCallback> functionCallbacks = new ArrayList<>(3);
        functionCallbacks.add(FunctionCallback.builder()
                .function("getOnlineUserInfo", new GetOnlineUserInfoTool())
                .description("查询在线用户")
                .build());
        return functionCallbacks;
    }
}
