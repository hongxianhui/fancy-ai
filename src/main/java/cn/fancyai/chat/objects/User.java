package cn.fancyai.chat.objects;

import cn.fancyai.chat.endpoint.SpeechWebSocketSessionDecorator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends MetadataSupport {
    @EqualsAndHashCode.Include
    private String userId;
    private Model model = new Model();
    @JsonIgnore
    private String apiKey;
    @JsonIgnore
    private SpeechWebSocketSessionDecorator chatSession;
}
