package cn.fancyai.chat.client.worker.flow;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.api.ImageGenerationAPI;
import cn.fancyai.chat.api.SpeechGenerationAPI;
import cn.fancyai.chat.api.TextGenerationAPI;
import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.objects.ChatUsage;
import cn.fancyai.chat.objects.User;
import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class GenerateClipVideoFlowWorker {
    private final String prompt;
    private final User user;
    private final File outputFile;

    private String videoPlan;
    private List<ClipFrame> clipFrames;
    private final ChatUsage chatUsage = ChatUsage.builder().build();

    public GenerateClipVideoFlowWorker(User user, String prompt, File outputFile) {
        this.prompt = prompt;
        this.user = user;
        this.outputFile = outputFile;
    }

    public GenerateClipVideoFlowWorker generateVideoPlan() throws Exception {
        user.getModel().setTool("qwen-plus");
        TextGenerationAPI textApi = ServerApplication.applicationContext.getBean("textGenerationAPI", TextGenerationAPI.class);
        videoPlan = textApi.generate(user, prompt, ChatUtils.getPrompt("clip-video-generator-prompt.txt"), chatUsage);
        return this;
    }

    public GenerateClipVideoFlowWorker formatVideoPlan() {
        List<ClipFrame> clipFrames = new ArrayList<>();
        ClipFrame clipFrame = null;
        for (String line : videoPlan.split("\n")) {
            if (line.startsWith("分镜头")) {
                clipFrame = new ClipFrame();
                clipFrame.setNo(Integer.parseInt(line.substring(3, 4)));
                continue;
            }
            if (line.startsWith("画面内容：")) {
                clipFrame.setImagePrompt(line.substring(line.indexOf("：") + 1));
                continue;
            }
            if (line.startsWith("解说词：")) {
                clipFrame.setSpeechText(line.substring(line.indexOf("：") + 1));
                clipFrames.add(clipFrame);
            }
        }
        this.clipFrames = clipFrames;
        return this;
    }

    public GenerateClipVideoFlowWorker generateImages(Consumer<ClipFrame> callback) throws Exception {
        user.getModel().setTool("wanx2.1-t2i-turbo");
        ImageGenerationAPI imageAPI = ServerApplication.applicationContext.getBean("imageGenerationAPI", ImageGenerationAPI.class);
        for (ClipFrame clipFrame : clipFrames) {
            String imageUrl = imageAPI.generate(user, clipFrame.getImagePrompt(), chatUsage);
            clipFrame.setImageUrl(imageUrl);
            callback.accept(clipFrame);
        }
        return this;
    }

    public GenerateClipVideoFlowWorker generateSpeech(Consumer<ClipFrame> callback) throws Exception {
        user.getModel().setTool("sambert-zhishu-v1");
        String tempFolder = ServerApplication.applicationContext.getEnvironment().getProperty("ai.tempfile.folder");
        SpeechGenerationAPI speechAPI = ServerApplication.applicationContext.getBean("speechGenerationAPI", SpeechGenerationAPI.class);
        for (ClipFrame clipFrame : clipFrames) {
            byte[] speechBytes = speechAPI.generate(user, clipFrame.getSpeechText(), chatUsage);
            IOUtils.copy(new ByteArrayInputStream(speechBytes), new FileOutputStream(tempFolder + File.separatorChar + "flow" + File.separatorChar + clipFrame.getNo() + ".mp3"));
            clipFrame.setAudio(speechBytes);
            callback.accept(clipFrame);
        }
        return this;
    }

    public GenerateClipVideoFlowWorker generateSubtitles(Consumer<ClipFrame> callback) throws Exception {
        SubtitleGenerator subtitleGenerator = new SubtitleGenerator();
        for (ClipFrame clipFrame : clipFrames) {
            BufferedImage bufferedImage = ImageIO.read(new URL(clipFrame.getImageUrl()).openStream());
            subtitleGenerator.drawCenteredWrappedText(
                    bufferedImage,
                    clipFrame.getSpeechText(),
                    bufferedImage.getHeight() - 100,
                    bufferedImage.getWidth() - 40,
                    20,
                    10
            );
            Thread.sleep(500);
            clipFrame.setImageData(bufferedImage);
            callback.accept(clipFrame);
        }
        return this;
    }

    public GenerateClipVideoFlowWorker generateVideo(Consumer<ClipFrame> videoCallback, Consumer<ClipFrame> audioCallback) throws Exception {
        VideoGenerator videoGenerator = new VideoGenerator();
        String tempFolder = ServerApplication.applicationContext.getEnvironment().getProperty("ai.tempfile.folder");
        File bgmAudioPath = new File(tempFolder + File.separatorChar + "flow" + File.separatorChar + "bgm.wav");
        videoGenerator.generate(bgmAudioPath, clipFrames, outputFile, videoCallback, audioCallback);
        return this;
    }

    @Getter
    @Setter
    public static class ClipFrame {
        int no;
        String imagePrompt;
        String speechText;
        String imageUrl;
        BufferedImage imageData;
        byte[] audio;
    }

}
