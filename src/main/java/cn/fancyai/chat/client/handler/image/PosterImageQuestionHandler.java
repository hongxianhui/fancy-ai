package cn.fancyai.chat.client.handler.image;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.tokenizer.PosterImageDescTokenizer;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.api.AsynchronousApi;
import com.alibaba.dashscope.base.HalfDuplexServiceParam;
import com.alibaba.dashscope.common.DashScopeResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.protocol.ApiServiceOption;
import com.alibaba.dashscope.protocol.HttpMethod;
import com.alibaba.dashscope.protocol.Protocol;
import com.alibaba.dashscope.protocol.StreamingMode;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Order(410)
public class PosterImageQuestionHandler extends AbstractImageQuestionHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private PosterImageDescTokenizer posterImageDescTokenizer;

    @Override
    protected String getModelName(Question question) {
        return "wanx-poster-generation-v1";
    }

    @Override
    protected Object checkQuestion(Question question) {
        if (!question.getContent().startsWith("生成海报：")) {
            return Boolean.FALSE;
        }
        Map<String, String> stringStringMap = posterImageDescTokenizer.extractKeyValues(question.getContent());
        if (stringStringMap.size() != 4) {
            return Answer.builder(question.getUser())
                    .content(ChatUtils.getConstant("poster.html"))
                    .done()
                    .build();
        }
        return Boolean.TRUE;
    }

    @Override
    protected ImageSynthesisResult call(Question question) throws NoApiKeyException {
        User user = question.getUser();
        Map<String, String> stringStringMap = posterImageDescTokenizer.extractKeyValues(question.getContent());
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
                .model(getModelName(question))
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
        ImageSynthesisResult result = ImageSynthesisResult.fromDashScopeResult(response);
        result.getOutput().setResults(List.of(Map.of("url", url)));
        return result;
    }
}
