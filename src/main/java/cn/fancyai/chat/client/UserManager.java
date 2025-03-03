package cn.fancyai.chat.client;

import cn.fancyai.chat.objects.User;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    public User userConnected(String userId) {
        User user = getUser(userId);
        if (user != null) {
            return user;
        }
        user = new User();
        user.setUserId(userId);
        user.getModel().setChat(defaultModel);
        users.add(user);
        return user;
    }

    public User userGone(String userId) throws IOException {
        for (User user : users) {
            if (userId.equals(user.getUserId())) {
                users.remove(user);
                return user;
            }
        }
        return null;
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
