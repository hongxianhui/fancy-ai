package cn.fancyai.chat.client.worker.flow;

import cn.fancyai.chat.ServerApplication;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC;
import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264;

public class VideoGenerator {
    protected final Logger logger = LoggerFactory.getLogger(VideoGenerator.class);

    public static final int FRAME_RATE = 25;
    public static final int TRANSFER_DURATION = FRAME_RATE * 2;
    public static final float bgVolume = 0.1F;

    public void generate(
            InputStream bgAudioSource,
            List<GenerateClipVideoFlowWorker.ClipFrame> clipFrames,
            File outputFile,
            Consumer<GenerateClipVideoFlowWorker.ClipFrame> videoCallback,
            Consumer<GenerateClipVideoFlowWorker.ClipFrame> audioCallback
    ) throws Exception {
        String tempFolder = ServerApplication.applicationContext.getEnvironment().getProperty("ai.tempfile.folder");
        FileInputStream silenceAudioSource = new FileInputStream(new File(tempFolder, "flow" + File.separatorChar + "silence.mp3"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(silenceAudioSource, byteArrayOutputStream);
        byte[] silenceAudioData = byteArrayOutputStream.toByteArray();
        List<BufferedImage> images = new ArrayList<>();
        List<byte[]> speeches = new ArrayList<>();
        for (GenerateClipVideoFlowWorker.ClipFrame clipFrame : clipFrames) {
            images.add(clipFrame.imageData);
            images.add(clipFrame.imageData);
            speeches.add(silenceAudioData);
            speeches.add(clipFrame.audio);
        }
        images.add(clipFrames.get(clipFrames.size() - 1).imageData);
        speeches.add(silenceAudioData);

        FFmpegFrameGrabber bgmAudioGrabber = new FFmpegFrameGrabber(bgAudioSource);
        bgmAudioGrabber.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 720, 1280, 1);
        recorder.setFormat("mp4");
        recorder.setSampleRate(bgmAudioGrabber.getSampleRate());
        recorder.setFrameRate(FRAME_RATE);
        recorder.setVideoCodec(AV_CODEC_ID_H264);
        recorder.setAudioCodec(AV_CODEC_ID_AAC);
        recorder.setVideoQuality(0);
        recorder.start();

        FFmpegFrameGrabber[] speechAudioGrabbers = new FFmpegFrameGrabber[images.size()];
        for (int i = 0; i < speeches.size(); i++) {
            speechAudioGrabbers[i] = new FFmpegFrameGrabber(new ByteArrayInputStream(speeches.get(i)));
            speechAudioGrabbers[i].start();
        }

        Java2DFrameConverter converter = new Java2DFrameConverter();
        for (int i = 0; i < images.size(); i++) {
            logger.info("Generating video clip: {}", (i + 1));
            double duration = speechAudioGrabbers[i].getLengthInTime() / 1000000.0;
            int totalFrames = (int) ((duration) * recorder.getFrameRate());
            BufferedImage imageData = images.get(i);
            for (int j = 0; j < totalFrames; j++) {
                BufferedImage frameImage = imageData;
                if (i > 0 && i % 2 == 0 && j < TRANSFER_DURATION) {
                    BufferedImage previousImage = images.get(i - 1);
                    Graphics2D g2d = previousImage.createGraphics();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float) j + 1) / TRANSFER_DURATION));
                    g2d.drawImage(imageData, 0, 0, null);
                    frameImage = previousImage;
                }
                recorder.record(converter.convert(frameImage));
            }
            imageData.getGraphics().dispose();
            if (i % 2 == 0 && i < speeches.size() - 1) {
                videoCallback.accept(clipFrames.get(i / 2));
            }
        }

        String filter = "[0:a]volume=" + bgVolume * 10 + "[aud1];[1:a]volume=10[aud2];[aud1][aud2]amix=inputs=2[a]";
        FFmpegFrameFilter frameFilter = new FFmpegFrameFilter(filter, recorder.getAudioChannels());
        frameFilter.setAudioInputs(2);
        frameFilter.setSampleRate(recorder.getSampleRate());
        frameFilter.start();

        for (int i = 0; i < speeches.size(); i++) {
            logger.info("Merging audio clip: {}", (i + 1));
            Frame audioFrame;
            while ((audioFrame = speechAudioGrabbers[i].grabFrame()) != null) {
                Frame bgmAudioFrame = bgmAudioGrabber.grabFrame();
                frameFilter.push(0, bgmAudioFrame);
                frameFilter.push(1, audioFrame);
                recorder.record(frameFilter.pull());
            }
            speechAudioGrabbers[i].close();
            if (i % 2 == 0 && i < speeches.size() - 1) {
                audioCallback.accept(clipFrames.get(i / 2));
            }
        }
        bgmAudioGrabber.close();
        recorder.close();
    }

    public static void main(String[] args) throws IOException {
        // 加载图片
        File file1 = new File("d:\\tempfile\\flow\\1.png");
        File file2 = new File("d:\\tempfile\\flow\\2.png");
        BufferedImage image1 = ImageIO.read(file1);
        BufferedImage image2 = ImageIO.read(file2);

        // 创建新的图像，大小与第一张图片相同
        BufferedImage combinedImage = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combinedImage.createGraphics();

        // 绘制第一张图片（不设置透明度）
        g2d.drawImage(image1, 0, 0, null);

        // 绘制第二张图片，设置半透明（例如50%透明度）
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // 设置50%透明度
        g2d.drawImage(image2, 0, 0, null); // 可以调整位置和大小

        // 释放资源
        g2d.dispose();

        // 保存或显示新图像
        File outputFile = new File("d:\\tempfile\\flow\\output.png");
        ImageIO.write(combinedImage, "PNG", outputFile); // 保存为PNG格式
    }
}
