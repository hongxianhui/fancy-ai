package cn.fancyai.chat.endpoint;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.client.handler.exception.ChatExceptionConsumer;
import cn.fancyai.chat.client.rag.QuerySequence;
import cn.fancyai.chat.client.rag.UploadSequence;
import cn.fancyai.chat.objects.User;
import com.aliyun.sdk.service.bailian20231229.models.ListFileResponseBody;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.List;

@RestController
public class RestEndpoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private UserManager userManager;
    @Resource
    private TemplateEngine tTemplateEngine;

    @GetMapping("/prompts")
    public List<String> prompts() {
        return List.of("选择聊天角色", "选择朗读音色", "关闭语音朗读", "图片相关功能", "管理知识库");
    }

    @GetMapping("/knowledge")
    public void listKnowledge(@RequestParam String userId) {
        User user = userManager.getUser(userId);
        try {
            List<ListFileResponseBody.FileList> fileList = new QuerySequence().listDocuments().close().getFileList();
            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            final String text;
            if (fileList.isEmpty()) {
                text = "知识库为空。";
            } else {
                Context context = new Context();
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
                text = tTemplateEngine.process("knowledge_list.html", context);
            }
            ChatUtils.sendMessage(user, text);
        } catch (Exception e) {
            new ChatExceptionConsumer(user).accept(e);
        }
    }


    @PostMapping("/knowledge")
    public void uploadKnowledge(@RequestParam MultipartFile file, @RequestParam String userId) throws IOException {
        if (file.isEmpty()) {
            return;
        }
        User user = userManager.getUser(userId);
        try {
            byte[] bytes = file.getBytes();
            Path path = Files.createTempFile("uploaded-", file.getOriginalFilename());
            Files.write(path, bytes, StandardOpenOption.TRUNCATE_EXISTING);
            new UploadSequence(path.toFile(), file.getOriginalFilename()).applyFileUploadLease().uploadFile().addFile().close();
            ChatUtils.sendMessage(user, "文件已添加至知识库，服务端正在分析，稍后可使用带有“搜索知识库”字样的提示词进行提问");
        } catch (Exception e) {
            new ChatExceptionConsumer(user).accept(e);
        }
    }

}
