package cn.fancyai.chat.client.worker.knowledge;

import com.aliyun.sdk.service.bailian20231229.models.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AddKnowledgeWorker extends KnowledgeWorker<AddKnowledgeWorker> {
    private String fileUploadLeaseId = null;
    private String xbailianExtra = null;
    private String contentType = null;
    private String method = null;
    private String url = null;

    public AddKnowledgeWorker applyFileUploadLease() throws ExecutionException, InterruptedException, IOException {
        FileInputStream fileInputStream = new FileInputStream(super.tempFile);
        String fileMD5 = DigestUtils.md5Hex(fileInputStream);
        ApplyFileUploadLeaseRequest applyFileUploadLeaseRequest = ApplyFileUploadLeaseRequest.builder()
                .fileName(super.fileName)
                .md5(fileMD5)
                .categoryId(CATEGORY_ID)
                .workspaceId(WORKSPACE_ID)
                .sizeInBytes(String.valueOf(super.fileLength))
                .build();
        CompletableFuture<ApplyFileUploadLeaseResponse> response = asyncClient.applyFileUploadLease(applyFileUploadLeaseRequest);
        ApplyFileUploadLeaseResponse applyFileUploadLeaseResponse = response.get();
        ApplyFileUploadLeaseResponseBody.Data data = applyFileUploadLeaseResponse.getBody().getData();
        fileUploadLeaseId = data.getFileUploadLeaseId();
        Map<String, String> headers = (Map<String, String>) data.getParam().getHeaders();
        xbailianExtra = headers.get("X-bailian-extra");
        contentType = headers.get("Content-Type");
        method = data.getParam().getMethod();
        url = data.getParam().getUrl();
        return this;
    }

    public AddKnowledgeWorker uploadKnowledge() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setRequestProperty("X-bailian-extra", xbailianExtra);
        connection.setRequestProperty("Content-Type", contentType);
        try (DataOutputStream outStream = new DataOutputStream(connection.getOutputStream()); FileInputStream fileInputStream = new FileInputStream(super.tempFile)) {
            byte[] buffer = new byte[5120];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.flush();
        }
        if (connection.getResponseCode() == 200) {
            return this;
        }
        throw new IOException("Upload file to cloud failed.");
    }

    public AddKnowledgeWorker addKnowledge() throws ExecutionException, InterruptedException {
        AddFileRequest addFileRequest = AddFileRequest.builder()
                .leaseId(fileUploadLeaseId)
                .parser("DASHSCOPE_DOCMIND")
                .workspaceId(WORKSPACE_ID)
                .categoryId(CATEGORY_ID)
                .build();
        CompletableFuture<AddFileResponse> response = asyncClient.addFile(addFileRequest);
        response.get();
        return this;
    }

}
