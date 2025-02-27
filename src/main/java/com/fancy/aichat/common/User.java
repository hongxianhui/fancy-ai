package com.fancy.aichat.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Setter
@Getter
public class User {
    private String userId;
    private String model;
    private boolean admin;
    @JsonIgnore
    private WebSocketSession session;
}
