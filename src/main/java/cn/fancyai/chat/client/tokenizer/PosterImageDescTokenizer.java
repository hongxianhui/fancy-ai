package cn.fancyai.chat.client.tokenizer;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PosterImageDescTokenizer {
    private final List<String> delimiters = List.of(":", "：", ";", "；");
    private final Set<String> keyStatements = Set.of("标题", "子标题", "内容", "提示词");

    public Map<String, String> extractKeyValues(String input) {
        // 构造正则表达式分割符‌:ml-citation{ref="1,2" data="citationList"}
        String delimiterRegex = "[" + String.join("", delimiters) + "]";
        // 分割并处理空字符串‌:ml-citation{ref="1,2" data="citationList"}
        List<String> sentences = Arrays.stream(input.split(delimiterRegex))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        Map<String, String> result = new HashMap<>();
        for (String key : keyStatements) {
            int index = sentences.indexOf(key);
            // 关键语句检查‌:ml-citation{ref="2" data="citationList"}
            if (index == -1) {
                return Collections.emptyMap();
            }
            // 后续语句检查‌:ml-citation{ref="2" data="citationList"}
            if (index + 1 >= sentences.size()) {
                return Collections.emptyMap();
            }
            result.put(key, sentences.get(index + 1));
        }
        return result;
    }
}
