package cn.fancyai.chat.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Answer extends Metadata {
    public static final String TYPE_THINK = "think";
    public static final String TYPE_ANSWER = "answer";
    public static final String TYPE_TOOL = "tool";
    public static final String TYPE_FLOW = "flow";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_NEW = "new";

    private String type = TYPE_ANSWER;
    private User user;
    private String content;
    private ChatUsage usage;
    private boolean done;

    public static Builder builder(User user) {
        return new Builder().user(user);
    }

    public static class Builder {
        private final Answer answer = new Answer();

        public Builder user(User user) {
            answer.user = user;
            return this;
        }

        public Builder type(String type) {
            answer.type = type;
            return this;
        }

        public Builder content(String content) {
            answer.content = content;
            return this;
        }

        public Builder usage(ChatUsage chatUsage) {
            answer.usage = chatUsage;
            return this;
        }

        public Builder done() {
            answer.done = true;
            return this;
        }

        public Builder metadata(String key, Object value) {
            answer.metadata.put(key, value);
            return this;
        }

        public Answer build() {
            return answer;
        }
    }

}
