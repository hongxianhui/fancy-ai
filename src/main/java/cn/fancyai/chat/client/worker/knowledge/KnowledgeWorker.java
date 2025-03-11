package cn.fancyai.chat.client.worker.knowledge;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.client.worker.UploadFileWorker;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.bailian20231229.AsyncClient;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.core.env.Environment;

public abstract class KnowledgeWorker<T> extends UploadFileWorker<T> {
    public static final String CATEGORY_ID = "cate_54146e8aa37a41ad90d215e5b7d67a36_11209711";
    public static final String WORKSPACE_ID = "llm-z6xbh2xfa9n1er6q";

    protected final AsyncClient asyncClient;

    public KnowledgeWorker() {
        Environment environment = ServerApplication.applicationContext.getEnvironment();
        String accessKeyId = environment.getProperty("ai.ailibaba-accessKeyId");
        String accessKeySecret = environment.getProperty("ai.ailibaba-accessKeySecret");
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .build());
        asyncClient = AsyncClient.builder().region("cn-beijing")
                .credentialsProvider(provider).
                overrideConfiguration(
                        ClientOverrideConfiguration.create().
                                setEndpointOverride("bailian.cn-beijing.aliyuncs.com")
                ).build();

    }

    public T close() {
        if (asyncClient != null) {
            asyncClient.close();
        }
        return (T) this;
    }
}
