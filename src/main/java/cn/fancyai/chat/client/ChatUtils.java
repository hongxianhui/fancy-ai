package cn.fancyai.chat.client;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.client.handler.exception.ChatExceptionConsumer;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.micrometer.common.util.StringUtils;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ResourceUtils;
import org.springframework.web.socket.TextMessage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {
    private static final Logger logger = LoggerFactory.getLogger(ChatUtils.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @SneakyThrows
    public static String serialize(Object obj) {
        return mapper.writeValueAsString(obj);
    }

    public static <T> T deserialize(String json, Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(json, clazz);
    }

    public static String getConstant(String resource) {
        String text = ResourceUtils.getText("classpath:/constant/" + resource);
        return text.replaceAll("\r\n", "").replaceAll("\n", "");
    }

    public static String getPrompt(String resource) {
        String text = ResourceUtils.getText("classpath:/prompt/" + resource);
        return text.replaceAll("\r\n", "").replaceAll("\n", "");
    }

    public static Map<String, String> parseQueryParams(String url) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        String[] urlParts = url.split("\\?", 2);
        String query = urlParts.length > 1 ? urlParts[1] : urlParts[0];

        // 正则表达式解析键值对
        Pattern pattern = Pattern.compile("([^&=]+)=([^&]*)");  // 匹配key=value结构‌:ml-citation{ref="2,3" data="citationList"}
        Matcher matcher = pattern.matcher(query);
        while (matcher.find()) {
            String key = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8);  // 解码参数‌:ml-citation{ref="1" data="citationList"}
            String value = matcher.group(2).isEmpty() ? null : URLDecoder.decode(matcher.group(2), StandardCharsets.UTF_8);
            params.put(key, value);
        }
        return params;
    }

    public static String getApiKey(User user) throws NoApiKeyException {
        String apiKey = user.getApiKey();
        if (Strings.isBlank(apiKey)) {
            apiKey = ServerApplication.applicationContext.getEnvironment().getProperty("ai.api-key.default");
        }
        if (StringUtils.isBlank(apiKey)) {
            apiKey = null;
        }
        return apiKey;
    }

    public static void sendMessage(User user, String message) throws IOException {
        message = message.replaceAll("\n", "<span class=\"token splitter\"></span>");
        sendMessage(user, message, Answer.TYPE_ANSWER);
    }

    public static void sendMessage(User user, String message, String answerType) throws IOException {
        Answer answer = Answer.builder(user).content(message).type(answerType).done().build();
        user.getChatSession().sendMessage(new TextMessage(serialize(answer)));
    }

    public static void sendMessageSilent(User user, String message, String answerType) {
        Answer answer = Answer.builder(user).content(message).type(answerType).done().build();
        try {
            user.getChatSession().sendMessage(new TextMessage(serialize(answer)));
        } catch (IOException e) {
            new ChatExceptionConsumer(user).accept(e);
        }
    }

    public static float getSpeechDuration(byte[] speechData) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(speechData));
            AudioFormat format = audioStream.getFormat();
            // 获取采样率（单位：Hz）
            float sampleRate = format.getSampleRate();
            // 计算总时长（单位：秒）
            long frameLength = audioStream.getFrameLength();
            float duration = frameLength / sampleRate;
            audioStream.close();
            return duration;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }
}
