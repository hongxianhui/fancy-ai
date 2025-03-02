package cn.fancyai.chat.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Builder
@Getter
@Setter
public class Answer {
    public static final String TYPE_THINK = "think";
    public static final String TYPE_ANSWER = "answer";
    public static final String TYPE_KEEPALIVE = "keepalive";
    public static final String TYPE_SPEECH = "speech";
    public static final String TYPE_TOOL = "tool";
    public static final String TYPE_IMAGE = "image";
    public static final String META_TTS_TOKENS = "TTS_TOKENS";

    @Builder.Default
    private String type = TYPE_ANSWER;
    private User user;
    private String content;
    private Usage usage;
    private boolean done;
    @JsonIgnore
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

}
