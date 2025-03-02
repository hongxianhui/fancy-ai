package cn.fancyai.chat.client.handler.question;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.client.handler.answer.AnswerCallback;
import cn.fancyai.chat.client.tokenizer.PosterImageDescTokenizer;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.Usage;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.api.AsynchronousApi;
import com.alibaba.dashscope.base.HalfDuplexServiceParam;
import com.alibaba.dashscope.common.DashScopeResult;
import com.alibaba.dashscope.protocol.ApiServiceOption;
import com.alibaba.dashscope.protocol.HttpMethod;
import com.alibaba.dashscope.protocol.Protocol;
import com.alibaba.dashscope.protocol.StreamingMode;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.util.List;
import java.util.Map;

@Component
@Order(70)
public class PosterQuestionHandler implements QuestionHandler {
    public static final String MODEL_NAME = "wanx-poster-generation-v1";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private PosterImageDescTokenizer posterImageDescTokenizer;

    @Override
    public boolean handle(Question question, AnswerCallback callback) throws Exception {
        User user = question.getUser();
        String content = question.getContent();
        if (!content.startsWith("生成海报：") && !content.startsWith("生成海报:")) {
            return false;
        }
        Map<String, String> stringStringMap = posterImageDescTokenizer.extractKeyValues(question.getContent());
        if (stringStringMap.size() != 4) {
            Answer answer = Answer.builder()
                    .user(question.getUser())
                    .content("请提供正确的格式提出画图要求，必须包含标题、子标题、内容和提示词，用冒号和分号做为分割，比如\n\n生成海报：标题：清明；子标题：袅绕青烟，穿越天上人间；内容：人间四月芳菲始，春归清明雨时节；提示词：朦胧远山，柳树，雨水，2D插画")
                    .done(true)
                    .build();
            callback.onAnswer(answer);
            return true;
        }
        Map<String, String> posterInput = Map.of(
                "generate_mode", "generate",
                "title", stringStringMap.get("标题"),
                "sub_title", stringStringMap.get("子标题"),
                "body_text", stringStringMap.get("内容"),
                "prompt_text_zh", stringStringMap.get("提示词"),
                "wh_ratios", "竖版"
        );
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .apiKey(ChatUtils.getApiKey(user))
                .model(MODEL_NAME)
                .extraInputs(posterInput)
                .prompt("")
                .build();
        DashScopeResult response = new AsynchronousApi<HalfDuplexServiceParam>().call(param, ApiServiceOption.builder()
                .protocol(Protocol.HTTP)
                .httpMethod(HttpMethod.POST)
                .streamingMode(StreamingMode.NONE)
                .taskGroup("aigc")
                .task("text2image")
                .function("image-synthesis")
                .isAsyncTask(true)
                .build());
        String url = ((JsonObject) response.getOutput()).getAsJsonArray("render_urls").getAsString();
        logger.info("Answer: {}", url);
        Answer answer = Answer.builder()
                .user(question.getUser())
                .type(Answer.TYPE_IMAGE)
                .content(ChatUtils.serialize(List.of(Map.of("url", url))))
//                .content("[{\"orig_prompt\":\"写实动物摄影，一只橘色小猫和白色小狗正在激烈打闹。小猫弓起背部，毛发竖立，露出尖锐牙齿，前爪试图扑向小狗。小狗则后腿站立，嘴巴张开欲咬，耳朵紧贴头部，尾巴高高翘起。两者动作定格在最具张力的瞬间。背景是温暖的午后庭院，阳光洒在地面形成自然光影。高清动态捕捉，近景特写，画面充满力量感与戏剧性冲突。\",\"actual_prompt\":\"写实动物摄影，一只橘色小猫和白色小狗正在激烈打闹。小猫弓起背部，毛发竖立，露出尖锐牙齿，前爪试图扑向小狗。小狗则后腿站立，嘴巴张开欲咬，耳朵紧贴头部，尾巴高高翘起。两者动作定格在最具张力的瞬间。背景是温暖的午后庭院，阳光洒在地面形成自然光影。高清动态捕捉，近景特写，画面充满力量感与戏剧性冲突。\",\"url\":\"https://dashscope-result-sh.oss-cn-shanghai.aliyuncs.com/1d/49/20250301/775d1686/4ef73132-89ef-4ae4-b366-1f9f6a57081f3261594598.png?Expires=1740913448&OSSAccessKeyId=LTAI5tKPD3TMqf2Lna1fASuh&Signature=o49OgS3333kleyoi2NCi69ThfDM%3D\"}]")
                .usage(Usage.builder().user(user).cost("限时免费").build())
                .done(true)
                .build();
        callback.onAnswer(answer);
        return true;
    }
}
