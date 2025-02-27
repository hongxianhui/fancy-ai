package com.fancy.aichat.manager;

import com.fancy.aichat.common.User;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("userManager")
public class UserManager {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

    @Getter
    @Setter
    @Value("${ai.default-model}")
    private String defaultModel;

    private final List<User> users = new ArrayList<>();

    public User userConnected(WebSocketSession session) throws IOException {
        User user = new User();
        String userId = session.getId();
        user.setUserId(userId);
        user.setSession(session);
        user.setModel(defaultModel);
        users.add(user);
        return user;
    }

    public User userGone(WebSocketSession session) throws IOException {
        for (User user : users) {
            String userId = user.getUserId();
            if (userId.equals(session.getId())) {
                users.remove(user);
                return user;
            }
        }
        return null;
    }

    public User updateUser(User user) {
        User localUser = getUser(user.getUserId());
        if (localUser == null) {
            return null;
        }
        localUser.setModel(user.getModel());
        return localUser;
    }

    public User getUser(String userId) {
        for (User user : users) {
            if (userId.equals(user.getUserId())) {
                return user;
            }
        }
        return null;
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

}
