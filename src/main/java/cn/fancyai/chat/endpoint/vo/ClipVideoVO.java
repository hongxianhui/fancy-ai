package cn.fancyai.chat.endpoint.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClipVideoVO {
    private String[] images;
    private String[] text;
    private String bgm = "http://www.fancy-ai.cn/download?fileName=/flow/bgm.wav";
    private String voice = "sambert-zhishu-v1";
}
