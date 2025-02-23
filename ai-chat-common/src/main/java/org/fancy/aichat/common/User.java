package org.fancy.aichat.common;

public class User {
    private String userId;
    private String model = "deepseek-r1:1.5b";

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
