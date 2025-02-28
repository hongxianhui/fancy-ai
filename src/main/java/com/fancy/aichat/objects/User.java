package com.fancy.aichat.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class User {
    public static final String META_VOICE = "VOICE";

    private String userId;
    private String model;
    private boolean admin;
    @JsonIgnore
    private WebSocketSession session;
    @JsonIgnore
    private Map<String, Object> metadata = new HashMap<>();
}
