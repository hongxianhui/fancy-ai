package cn.fancyai.chat.objects;

import cn.fancyai.chat.endpoint.SpeechSessionDecorator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends Metadata {
    @EqualsAndHashCode.Include
    private String userId;
    private Model model = new Model();
    @JsonIgnore
    private String apiKey;
    @JsonIgnore
    private SpeechSessionDecorator chatSession;
}
