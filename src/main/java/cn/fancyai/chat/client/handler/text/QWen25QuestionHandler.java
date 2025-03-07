package cn.fancyai.chat.client.handler.text;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.tools.ChatTool;
import cn.fancyai.chat.objects.Question;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeStoreOptions;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.mcp.client.McpSyncClient;
import org.springframework.ai.mcp.client.stdio.ServerParameters;
import org.springframework.ai.mcp.client.stdio.StdioClientTransport;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.mcp.spring.McpFunctionCallback;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(520)
public class QWen25QuestionHandler extends AbstractStreamingTextQuestionHandler {
    private final Logger logger = LoggerFactory.getLogger(QWen25QuestionHandler.class);

    @Value("${ai.api-key.public}")
    private String apiKeyPublic;

    private final McpSyncClient mcpClient;

    public QWen25QuestionHandler(
            @Value("${ai.mcp.command-path:C:\\Users\\Administrator\\.local\\bin\\uvx}")
            String commandPath,
            @Value("${ai.mcp.database-path:D:\\sqllite\\mcp.db}")
            String databasePath
    ) {
        ServerParameters stdioParams = ServerParameters.builder(commandPath)
                .args("mcp-server-sqlite", "--db-path", databasePath)
                .build();
        mcpClient = McpClient.using(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(10)).sync();
        McpSchema.InitializeResult init = mcpClient.initialize();
        logger.info("MCP Initialized: {}", init);
    }

    @Override
    protected String getModelName() {
        return "qwen2.5-1.5b-instruct";
    }

    @Override
    protected String getAPIKey(Question question) throws NoApiKeyException {
        String apiKey = super.getAPIKey(question);
        if (!StringUtils.hasText(apiKey)) {
            apiKey = apiKeyPublic;
        }
        return apiKey;
    }

    @Override
    protected List<Advisor> additionalAdvisors(Question question) throws NoApiKeyException {
        if (question.getContent().contains("搜索知识库")) {
            DashScopeCloudStore vectorStore = new DashScopeCloudStore(new DashScopeApi(getAPIKey(question)), new DashScopeStoreOptions("演示知识库"));
            return List.of(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().build(), ChatUtils.getPrompt("rag.txt")));
        }
        return super.additionalAdvisors(question);
    }

    @Override
    protected List<FunctionCallback> additionalFunctions(Question question) {
        List<FunctionCallback> functionCallbacks = new ArrayList<>();
        functionCallbacks.addAll(super.additionalFunctions(question));
        functionCallbacks.addAll(ChatTool.generateFunctionCallbacks());
        if (question.getContent().contains("调用数据库")) {
            functionCallbacks.addAll(mcpClient.listTools(null)
                    .tools()
                    .stream()
                    .map(tool -> new McpFunctionCallback(mcpClient, tool))
                    .toList());
        }
        return functionCallbacks;
    }

}
