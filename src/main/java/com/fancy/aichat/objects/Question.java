package com.fancy.aichat.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class Question {
    public static final String META_NO_THINK = "NO_THINK";
    public static final String META_IS_THINKING = "IS_THINKING";

    private User user;
    private String content;
    @JsonIgnore
    private Map<String, Object> metadata = new HashMap<>();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        Question question = new Question();

        public Builder user(User user) {
            question.user = user;
            return this;
        }

        public Builder content(String content) {
            question.content = content;
            return this;
        }

        public Question build() {
            return question;
        }
    }

}
