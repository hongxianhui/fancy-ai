package org.fancy.aichat.common;

public class Question {
    private User user;
    private ChatPrompt prompt;
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

        public Builder prompt(ChatPrompt prompt) {
            question.prompt = prompt;
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

    public ChatPrompt getPrompt() {
        return prompt;
    }

    public void setPrompt(ChatPrompt prompt) {
        this.prompt = prompt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
