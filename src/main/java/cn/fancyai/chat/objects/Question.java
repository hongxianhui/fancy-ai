package cn.fancyai.chat.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class Question extends MetadataSupport {
    public static final String META_NO_THINK = "NO_THINK";
    public static final String META_IS_THINKING = "IS_THINKING";

    private User user;
    private String content;

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

        public Builder metadata(String key, Object value) {
            question.metadata.put(key, value);
            return this;
        }

        public Question build() {
            return question;
        }
    }

}
