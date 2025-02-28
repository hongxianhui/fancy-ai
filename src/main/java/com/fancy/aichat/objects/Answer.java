package com.fancy.aichat.objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Answer {
    public static final String TYPE_THINK = "think";
    public static final String TYPE_ANSWER = "answer";
    public static final String TYPE_KEEPALIVE = "keepalive";
    public static final String TYPE_SPEECH = "speech";
    public static final String TYPE_TOOL = "tool";

    @Builder.Default
    private String type = TYPE_ANSWER;
    private User user;
    private String content;
    private Usage usage;
    private boolean done;

}
