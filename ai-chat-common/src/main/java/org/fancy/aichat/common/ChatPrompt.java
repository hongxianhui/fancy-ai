package org.fancy.aichat.common;

public enum ChatPrompt {
    ASK("聊天", "输入消息...", true),
    SHOW_HELP("了解系统功能", "帮助我了解系统功能", true),
    SWITCH_MODEL("切换模型", "输入qwen或者deepseek切换模型...", true),
    ADD_KNOWLEDGE("学习知识", "输入需要AI记住的知识...", true),
    USE_KNOWLEDGE("搜索知识", "输入需要AI搜索知识回答的问题...", true),

    USER_CONNECTED("用户连接", null, false),
    USER_GONE("用户离开", null, false),
    KEEPALIVE("心跳信息", null, false);

    private String placeHolder;
    private String prompt;
    private boolean user;

    ChatPrompt(String prompt, String placeHolder, boolean isUser) {
        this.prompt = prompt;
        this.placeHolder = placeHolder;
        this.user = isUser;
    }

    public boolean isUser() {
        return user;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }
}
