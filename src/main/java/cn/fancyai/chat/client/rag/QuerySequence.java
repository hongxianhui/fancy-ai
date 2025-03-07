package cn.fancyai.chat.client.rag;

import com.aliyun.sdk.service.bailian20231229.models.ListFileRequest;
import com.aliyun.sdk.service.bailian20231229.models.ListFileResponse;
import com.aliyun.sdk.service.bailian20231229.models.ListFileResponseBody;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Getter
public class QuerySequence extends KnowledgeSequence<QuerySequence> {

    private List<ListFileResponseBody.FileList> fileList;

    public QuerySequence listDocuments() throws ExecutionException, InterruptedException {
        ListFileRequest listFileRequest = ListFileRequest.builder()
                .workspaceId(WORKSPACE_ID)
                .categoryId(CATEGORY_ID)
                .build();

        CompletableFuture<ListFileResponse> response = asyncClient.listFile(listFileRequest);
        ListFileResponse resp = response.get();
        fileList = resp.getBody().getData().getFileList();
        return this;
    }


}
