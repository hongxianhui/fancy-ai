package cn.fancyai.chat.client.handler;

import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;

public class HandlerContext extends HashMap<String, Object> {

    public static final String SPEECH_MUTE = "speechMute";
    public static final String SPEECH_TOKENS = "speechTokens";
    public static final String SPEECH_TOKENIZER = "speechTokenizer";
    public static final String SPEECH_SESSION = "speechSession";

    @Override
    public Object put(String key, Object value) {
        return super.put(key, value);
    }

    public void mute() {
        put(HandlerContext.SPEECH_MUTE, Boolean.TRUE);
    }

    public boolean isMuted() {
        return Boolean.TRUE.equals(get(HandlerContext.SPEECH_MUTE));
    }

    public void unmute() {
        remove(HandlerContext.SPEECH_MUTE);
    }

    public void setSpeechSession(WebSocketSession session) {
        put(HandlerContext.SPEECH_SESSION, session);
    }

    public WebSocketSession getSpeechSession() {
        return (WebSocketSession) get(HandlerContext.SPEECH_SESSION);
    }
}
