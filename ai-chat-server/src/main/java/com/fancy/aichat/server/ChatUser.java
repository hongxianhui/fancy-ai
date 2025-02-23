package com.fancy.aichat.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.fancy.aichat.common.User;
import org.springframework.web.socket.WebSocketSession;

public class ChatUser extends User {
    @JsonIgnore
    private WebSocketSession session;

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }
}
