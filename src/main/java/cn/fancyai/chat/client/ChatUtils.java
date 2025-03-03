package cn.fancyai.chat.client;

import cn.fancyai.chat.ServerApplication;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ResourceUtils;
import org.springframework.web.socket.TextMessage;

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

    public static String serialize(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public static <T> T deserialize(String json, Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(json, clazz);
    }

    public static String getText(String resource) {
        String text = ResourceUtils.getText("classpath:/constant/" + resource);
        return text.replaceAll("\r\n", "");
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
            String value = matcher.group(2).isEmpty() ? "" : URLDecoder.decode(matcher.group(2), StandardCharsets.UTF_8);
            params.put(key, value);
        }
        return params;
    }

    public static String getApiKey(User user) throws NoApiKeyException {
        String apiKey = user.getApiKey();
        if (Strings.isBlank(apiKey)) {
            apiKey = ServerApplication.applicationContext.getEnvironment().getProperty("spring.ai.dashscope.api-key");
        }
        if (Strings.isBlank(apiKey)) {
            throw new NoApiKeyException();
        }
        return apiKey;
    }
}
