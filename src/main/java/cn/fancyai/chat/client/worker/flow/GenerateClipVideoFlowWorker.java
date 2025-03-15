package cn.fancyai.chat.client.worker.flow;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.api.ImageGenerationAPI;
import cn.fancyai.chat.api.SpeechGenerationAPI;
import cn.fancyai.chat.api.TextGenerationAPI;
import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.objects.APIUser;
import cn.fancyai.chat.objects.ChatUsage;
import cn.fancyai.chat.objects.User;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class GenerateClipVideoFlowWorker {
    private final String prompt;
    private final User user;
    private final File outputVideoFile;

    private String videoPlan;
    private List<ClipFrame> clipFrames;
    private final ChatUsage chatUsage = ChatUsage.builder().build();

    @Resource
    private SpeechGenerationAPI speechAPI;

    public GenerateClipVideoFlowWorker(User user, String prompt, File outputVideoFile) {
        this.prompt = prompt;
        this.user = user;
        this.outputVideoFile = outputVideoFile;
    }


    public GenerateClipVideoFlowWorker(APIUser user, String[] imageUrls, String[] speeches, File outputVideoFile) {
        this.user = user;
        this.outputVideoFile = outputVideoFile;
        this.prompt = null;
        this.clipFrames = new ArrayList<>(imageUrls.length);
        for (int i = 0; i < imageUrls.length; i++) {
            clipFrames.add(new ClipFrame(i + 1, speeches[i], imageUrls[i]));
        }
    }

    public GenerateClipVideoFlowWorker generateVideoPlan(String model) throws Exception {
        user.getModel().setTool(model);
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

    public GenerateClipVideoFlowWorker generateImages(String model, Consumer<ClipFrame> callback) throws Exception {
        user.getModel().setTool(model);
        ImageGenerationAPI imageAPI = ServerApplication.applicationContext.getBean("imageGenerationAPI", ImageGenerationAPI.class);
        for (ClipFrame clipFrame : clipFrames) {
            String imageUrl = imageAPI.generate(user, clipFrame.getImagePrompt(), chatUsage);
            clipFrame.setImageUrl(imageUrl);
            callback.accept(clipFrame);
        }
        return this;
    }

    public GenerateClipVideoFlowWorker generateSpeech(String model) throws Exception {
        return generateSpeech(model, clipFrame -> {
        });
    }

    public GenerateClipVideoFlowWorker generateSpeech(String model, Consumer<ClipFrame> callback) throws Exception {
        user.getModel().setTool(model);
        SpeechGenerationAPI speechAPI = ServerApplication.applicationContext.getBean("speechGenerationAPI", SpeechGenerationAPI.class);
        for (ClipFrame clipFrame : clipFrames) {
            byte[] speechBytes = speechAPI.generate(user, clipFrame.getSpeechText(), chatUsage);
            clipFrame.setAudio(speechBytes);
            callback.accept(clipFrame);
        }
        return this;
    }

    public GenerateClipVideoFlowWorker generateSubtitles() throws Exception {
        return generateSubtitles(clipFrame -> {
        });
    }

    public GenerateClipVideoFlowWorker generateSubtitles(Consumer<ClipFrame> callback) throws Exception {
        SubtitleGenerator subtitleGenerator = new SubtitleGenerator();
        for (ClipFrame clipFrame : clipFrames) {
//            BufferedImage bufferedImage = ImageIO.read(new URL(clipFrame.getImageUrl()).openStream());
            BufferedImage bufferedImage = ImageIO.read(new FileInputStream("D:\\tempfile\\flow\\" + clipFrame.getNo() + ".png"));
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

    public GenerateClipVideoFlowWorker generateVideo(InputStream bgmAudioSource) throws Exception {
        return generateVideo(bgmAudioSource, clipFrame -> {
        }, clipFrame -> {
        });
    }

    public GenerateClipVideoFlowWorker generateVideo(InputStream bgmAudioSource, Consumer<ClipFrame> videoCallback, Consumer<ClipFrame> audioCallback) throws Exception {
        new VideoGenerator().generate(bgmAudioSource, clipFrames, outputVideoFile, videoCallback, audioCallback);
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

        public ClipFrame() {
        }

        public ClipFrame(int no, String speechText, String imageUrl) {
            this.no = no;
            this.speechText = speechText;
            this.imageUrl = imageUrl;
        }
    }

}
