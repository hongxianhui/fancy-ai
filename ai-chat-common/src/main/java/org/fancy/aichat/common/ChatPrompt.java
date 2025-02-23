package org.fancy.aichat.common;

public enum ChatPrompt {
    ASK("ASK", "普通对话", true),
    SHOW_HELP("SHOW_HELP", "了解系统功能", true),
    SWITCH_MODEL("SWITCH_MODEL", "切换模型", true),
    ADD_KNOWLEDGE("ADD_KNOWLEDGE", "学习知识", true),
    USE_KNOWLEDGE("USE_KNOWLEDGE", "参考知识回答", true),
    CALL_FUNCTION("CALL_FUNCTION", "调用内置函数", true),

    USER_CONNECTED("USER_CONNECTED", "用户连接", false),
    USER_GONE("USER_GONE", "用户离开", false),
    KEEPALIVE("KEEPALIVE", "心跳信息", false);

    private String key;
    private String prompt;
    private boolean user;

    ChatPrompt(String key, String prompt, boolean isUser) {
        this.key = key;
        this.prompt = prompt;
        this.user = isUser;
    }

    public boolean isUser() {
        return user;
    }

    public String getKey() {
        return key;
    }

    public String getPrompt() {
        return prompt;
    }
}
