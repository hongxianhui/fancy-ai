package cn.fancyai.chat.client.worker;

import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.User;
import com.aliyun.sdk.service.bailian20231229.models.ListFileRequest;
import com.aliyun.sdk.service.bailian20231229.models.ListFileResponse;
import com.aliyun.sdk.service.bailian20231229.models.ListFileResponseBody;
import lombok.Getter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Getter
public class ListKnowledgeWorker extends KnowledgeWorker<ListKnowledgeWorker> {

    private List<ListFileResponseBody.FileList> fileList;
    private Answer answer;

    public ListKnowledgeWorker listDocuments() throws ExecutionException, InterruptedException {
        ListFileRequest listFileRequest = ListFileRequest.builder()
                .workspaceId(WORKSPACE_ID)
                .categoryId(CATEGORY_ID)
                .build();

        CompletableFuture<ListFileResponse> response = asyncClient.listFile(listFileRequest);
        ListFileResponse resp = response.get();
        fileList = resp.getBody().getData().getFileList();
        return this;
    }

    public ListKnowledgeWorker answer(User user, TemplateEngine templateEngine) {
        final String text;
        if (fileList.isEmpty()) {
            text = "知识库为空。";
        } else {
            Context context = new Context();
            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            context.setVariable("documents", fileList.stream().map(f -> {
                ListFileResponseBody.FileList.Builder builder = ListFileResponseBody.FileList.builder()
                        .createTime(f.getCreateTime().substring(5, 10).replaceAll("-", "/"))
                        .status("PARSE_SUCCESS".equals(f.getStatus()) ? "已解析" : "解析中")
                        .fileName(f.getFileName())
                        .fileType(f.getFileType());
                Long size = f.getSizeInBytes();
                if (size > 1024 * 1024) {
                    builder.categoryId(decimalFormat.format(size / 1024 / 1024) + "M");
                } else if (size > 1024) {
                    builder.categoryId(decimalFormat.format(size / 1024) + "K");
                } else {
                    builder.categoryId(size + "B");
                }
                return builder.build();
            }).toList());
            text = templateEngine.process("knowledge_list.html", context);
        }
        answer = Answer.builder(user).content(text).done().build();
        return this;
    }

}
