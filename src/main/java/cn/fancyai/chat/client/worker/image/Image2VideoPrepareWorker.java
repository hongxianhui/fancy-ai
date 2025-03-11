package cn.fancyai.chat.client.worker.image;

import cn.fancyai.chat.client.worker.UploadFileWorker;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.User;
import lombok.Getter;

@Getter
public class Image2VideoPrepareWorker extends UploadFileWorker<Image2VideoPrepareWorker> {
    private Answer answer;

    public Image2VideoPrepareWorker generateAnswer(String model, User user) {
        answer = Answer.builder(user)
                .content("图片已上传，请按输入框给出的格式编辑并提交提示词（注意：只修改提示词部分，其它部分不要动）。")
                .type(Answer.TYPE_ANSWER)
                .done()
                .build();
        return this;
    }
}
