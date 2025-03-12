package cn.fancyai.chat.objects;

import cn.fancyai.chat.ServerApplication;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class APIUser extends User {
    public static final String USER_ID = "api_user";

    public APIUser(String apiKey) {
        setUserId(USER_ID);
        setApiKey(apiKey);
    }
}
