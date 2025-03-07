package cn.fancyai.chat.objects;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Question extends Metadata {
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
