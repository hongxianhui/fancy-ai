package cn.fancyai.chat.client.worker;

import cn.fancyai.chat.ServerApplication;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Getter
public class UploadFileWorker<T> {
    protected File tempFile;
    protected String fileName;
    protected long fileLength;

    public T upload(MultipartFile uploadFile) throws IOException {
        String folder = ServerApplication.applicationContext.getEnvironment().getProperty("ai.tempfile.folder");
        fileName = uploadFile.getOriginalFilename();
        assert folder != null;
        assert fileName != null;
        Path filePath = Path.of(folder, fileName);
        byte[] bytes = uploadFile.getBytes();
        Files.write(Files.createFile(filePath), bytes, StandardOpenOption.TRUNCATE_EXISTING);
        tempFile = filePath.toFile();
        fileLength = tempFile.length();
        return (T) this;
    }
}
