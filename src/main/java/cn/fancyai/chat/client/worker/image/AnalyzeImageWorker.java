package cn.fancyai.chat.client.worker.image;

import cn.fancyai.chat.client.worker.UploadFileWorker;
import lombok.Getter;

@Getter
public class AnalyzeImageWorker extends UploadFileWorker<AnalyzeImageWorker> {
    private String question;

    public AnalyzeImageWorker generateQuestion() {
        question = "<img alt='' class='image' src='/image/" + fileName + "'><span class='token splitter'></span>帮我理解一下这张图片的内容";
        return this;
    }
}
