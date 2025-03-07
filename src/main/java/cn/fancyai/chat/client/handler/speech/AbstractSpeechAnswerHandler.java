package cn.fancyai.chat.client.handler.speech;

import cn.fancyai.chat.client.handler.AnswerHandler;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.tokenizer.TTSSentenceTokenizer;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.ChatUsage;
import cn.fancyai.chat.objects.User;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractSpeechAnswerHandler implements AnswerHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSpeechAnswerHandler.class);

    protected abstract String getModelName(Answer answer);

    protected abstract byte[] speech(Answer answer, String sentence) throws NoApiKeyException;

    @Override
    public boolean handle(Answer answer, HandlerContext context) throws NoApiKeyException, IOException {
        User user = answer.getUser();
        if (user.getModel().getSpeech() == null || context.isMuted()) {
            return true;
        }
        if (!user.getModel().getSpeech().equals(getModelName(answer))) {
            return false;
        }
        WebSocketSession speechSession = context.getSpeechSession();
        if (speechSession == null) {
            logger.warn("Speech session for user {} not found", user.getUserId());
            return true;
        }
        AtomicInteger speechTokens = (AtomicInteger) context.get(HandlerContext.SPEECH_TOKENS);
        if (speechTokens == null) {
            context.put(HandlerContext.SPEECH_TOKENS, speechTokens = new AtomicInteger());
        }
        logger.info("Handle answer: {}::{}", getClass().getSimpleName(), getModelName(answer));
        speechTokens.addAndGet(answer.getContent().length());
        TTSSentenceTokenizer sentenceTokenizer = (TTSSentenceTokenizer) context.get(HandlerContext.SPEECH_TOKENIZER);
        if (sentenceTokenizer == null) {
            context.put(HandlerContext.SPEECH_TOKENIZER, sentenceTokenizer = new TTSSentenceTokenizer(10));
        }
        String content = answer.getContent();
        String sentence = sentenceTokenizer.tokenize(content);
        if (!answer.isDone() && !StringUtils.hasText(sentence)) {
            return true;
        }
        if (StringUtils.hasText(sentence)) {
            speechSession.sendMessage(new BinaryMessage(speech(answer, sentence)));
        }
        if (answer.isDone()) {
            String remaining = sentenceTokenizer.getRemaining();
            if (StringUtils.hasText(remaining)) {
                speechSession.sendMessage(new BinaryMessage(speech(answer, remaining)));
            }
            ChatUsage chatUsage = answer.getUsage();
            if (chatUsage == null) {
                chatUsage = ChatUsage.builder().user(user).build();
                answer.setUsage(chatUsage);
            }
            chatUsage.setSpeechTokens(speechTokens.get());
        }
        return true;
    }

}
