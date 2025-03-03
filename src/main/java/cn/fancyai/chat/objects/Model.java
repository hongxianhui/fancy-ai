package cn.fancyai.chat.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Model {
    private String chat = "qwen2.5:0.5b";
    private String speech;
    private String image;
    private String video;
}
