package com.fancy.aichat.common;

import lombok.Data;

@Data
public class Answer {
    public static final String TYPE_THINK = "think";
    public static final String TYPE_ANSWER = "answer";
    public static final String TYPE_KEEPALIVE = "keepalive";
    public static final String TYPE_SPEECH = "speech";

    private User user;
    private String type;
    private String content;
    private boolean done;

    public static Answer.Builder builder() {
        return new Answer.Builder();
    }

    public static class Builder {
        Answer answer = new Answer();

        private Builder() {
            answer.setType(TYPE_ANSWER);
        }

        public Answer.Builder user(User user) {
            answer.user = user;
            return this;
        }

        public Answer.Builder type(String type) {
            answer.type = type;
            return this;
        }

        public Answer.Builder content(String content) {
            answer.content = content;
            return this;
        }

        public Answer.Builder done() {
            answer.done = true;
            return this;
        }

        public Answer build() {
            return answer;
        }
    }

}
