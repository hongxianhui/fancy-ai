package cn.fancyai.chat.client.rag;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.bailian20231229.AsyncClient;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.beans.factory.annotation.Value;

public abstract class KnowledgeSequence<T> {
    public static final String CATEGORY_ID = "cate_54146e8aa37a41ad90d215e5b7d67a36_11209711";
    public static final String WORKSPACE_ID = "llm-z6xbh2xfa9n1er6q";

    @Value("${ai.ailibaba-accessKeyId}")
    private String accessKeyId;
    @Value("${ai.ailibaba-accessKeySecret}")
    private String accessKeySecret;

    protected final AsyncClient asyncClient;

    public KnowledgeSequence() {
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
