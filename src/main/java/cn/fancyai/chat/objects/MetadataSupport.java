package cn.fancyai.chat.objects;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class MetadataSupport {
    protected Map<String, Object> metadata = new HashMap<>();
}
