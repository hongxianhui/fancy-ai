package com.fancy.aichat.server.user;

import org.fancy.aichat.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class UserManager {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

    private final List<ChatUser> users = new ArrayList<>();

    public ChatUser userConnected(WebSocketSession session) throws IOException {
        ChatUser user = new ChatUser();
        String userId = session.getId();
        user.setUserId(userId);
        user.setSession(session);
        users.add(user);
        return user;
    }

    public ChatUser userGone(WebSocketSession session) throws IOException {
        for (ChatUser user : users) {
            String userId = user.getUserId();
            if (userId.equals(session.getId())) {
                users.remove(user);
                return user;
            }
        }
        return null;
    }

    public ChatUser updateUser(User user) {
        ChatUser localUser = getUser(user.getUserId());
        if (localUser == null) {
            return null;
        }
        localUser.setModel(user.getModel());
        return localUser;
    }

    public ChatUser getUser(String userId) {
        for (ChatUser user : users) {
            if (userId.equals(user.getUserId())) {
                return user;
            }
        }
        return null;
    }

    public List<ChatUser> getUsers() {
        return Collections.unmodifiableList(users);
    }

}
