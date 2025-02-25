package org.fancy.aichat.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

}
