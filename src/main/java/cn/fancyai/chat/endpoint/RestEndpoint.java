package cn.fancyai.chat.endpoint;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.UserManager;
import cn.fancyai.chat.client.handler.exception.ChatExceptionConsumer;
import cn.fancyai.chat.client.worker.flow.GenerateClipVideoFlowWorker;
import cn.fancyai.chat.client.worker.image.AnalyzeImageWorker;
import cn.fancyai.chat.client.worker.image.Image2VideoPrepareWorker;
import cn.fancyai.chat.client.worker.knowledge.AddKnowledgeWorker;
import cn.fancyai.chat.client.worker.knowledge.ListKnowledgeWorker;
import cn.fancyai.chat.endpoint.vo.ClipVideoVO;
import cn.fancyai.chat.objects.APIUser;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.User;
import com.aliyun.core.utils.IOUtils;
import com.aliyun.core.utils.StringUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.socket.TextMessage;
import org.thymeleaf.TemplateEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@RestController
public class RestEndpoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private UserManager userManager;
    @Resource
    private TemplateEngine templateEngine;
    @Value("${ai.tempfile.folder}")
    private String tempFileFolder;

    @GetMapping("/prompts")
    public List<String> prompts() {
        return List.of("选择聊天角色", "选择朗读音色", "关闭语音朗读", "图片相关功能", "视频相关功能", "管理知识库", "智能体示例");
    }

    @PostMapping("/analyzeImage")
    public String analyzeImage(@RequestParam MultipartFile file, @RequestParam String userId, @RequestParam String model) throws IOException {
        return new AnalyzeImageWorker().upload(file).generateQuestion().getQuestion();
    }

    @PostMapping("/prepareImage2video")
    public String prepareImage2video(@RequestParam MultipartFile file, @RequestParam String userId, @RequestParam String model) throws IOException {
        User user = userManager.getUser(userId);
        Image2VideoPrepareWorker worker = new Image2VideoPrepareWorker().upload(file).generateAnswer(model, user);
        Answer answer = worker.getAnswer();
        String fileName = worker.getFileName();
        user.getChatSession().sendMessage(new TextMessage(ChatUtils.serialize(answer)));
        return "图生视频（" + model + "）：\n图片：" + fileName + "\n提示词：让人物跳舞。";
    }

    @GetMapping("/knowledge")
    public void listKnowledge(@RequestParam String userId) {
        User user = userManager.getUser(userId);
        try {
            Answer answer = new ListKnowledgeWorker().listDocuments().answer(user, templateEngine).close().getAnswer();
            user.getChatSession().sendMessage(new TextMessage(ChatUtils.serialize(answer)));
        } catch (Exception e) {
            new ChatExceptionConsumer(user).accept(e);
        }
    }

    @PostMapping("/knowledge")
    public void uploadKnowledge(@RequestParam MultipartFile file, @RequestParam String userId) {
        User user = userManager.getUser(userId);
        try {
            new AddKnowledgeWorker().upload(file).applyFileUploadLease().uploadKnowledge().addKnowledge().close();
            ChatUtils.sendMessage(user, "文件已添加至知识库，服务端正在分析，稍后可使用带有“搜索知识库”字样的提示词进行提问");
        } catch (Exception e) {
            new ChatExceptionConsumer(user).accept(e);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestParam String fileName) throws IOException {
        if (fileName.contains("..")) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(tempFileFolder, fileName);
        if (!file.exists()) {
            throw new RuntimeException("File not found");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Files.probeContentType(file.toPath())))
                .body(outputStream -> IOUtils.copy(new FileInputStream(file), outputStream));
    }

    @PostMapping("/api/v1/clip")
    public ResponseEntity<Map<String, Object>> createClipVideo(@RequestBody ClipVideoVO clipVideoVO, @RequestParam String token) {
        if (!token.equals("www.fancy-ai.cn")) {
            return ResponseEntity.ok(Map.of("success", Boolean.FALSE, "data", "token值不正确"));
        }
        String[] images = clipVideoVO.getImages();
        String[] text = clipVideoVO.getText();
        String bgm = clipVideoVO.getBgm();
        String voice = clipVideoVO.getVoice();

        if (StringUtils.isBlank(bgm)) {
            bgm = "http://fancy-ai.cn/download?fileName=/flow/bgm.wav";
        }

        if (StringUtils.isBlank(voice)) {
            voice = "sambert-zhishu-v1";
        }

        if (images.length == 0 || text.length == 0 || images.length != text.length) {
            return ResponseEntity.ok(Map.of("success", Boolean.FALSE, "data", "参数不正确，图片和解说词地址URL不能为空，且数量需一致。"));
        }

        if (images.length > 10) {
            return ResponseEntity.ok(Map.of("success", Boolean.FALSE, "data", "参数不正确，最多支持10个图片。"));
        }

        String outputVideoFileName = "clipShow-" + System.currentTimeMillis() + ".mp4";
        try {
            File outputVideoFile = new File(tempFileFolder, outputVideoFileName);
            InputStream bgmAudioSource = new URL(bgm).openStream();
            String publicApiKey = ServerApplication.applicationContext.getEnvironment().getProperty("ai.api-key.public");
            new GenerateClipVideoFlowWorker(new APIUser(publicApiKey), images, text, outputVideoFile)
                    .generateSpeech(voice)
                    .generateSubtitles()
                    .generateVideo(bgmAudioSource);
        } catch (Exception e) {
            logger.error("generate clip video failed.", e);
            return ResponseEntity.ok(Map.of("success", Boolean.FALSE, "data", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("success", Boolean.TRUE, "data", "http://fancy-ai.cn/download?fileName=/flow/" + outputVideoFileName));
    }
}