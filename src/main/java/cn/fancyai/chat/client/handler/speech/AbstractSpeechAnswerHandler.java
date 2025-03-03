package cn.fancyai.chat.client.handler.speech;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.AnswerHandler;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.tokenizer.TTSSentenceTokenizer;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Usage;
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

    protected abstract byte[] speech(User user, String text) throws NoApiKeyException;

    @Override
    public boolean handle(Answer answer, HandlerContext context) throws NoApiKeyException, IOException {
        User user = answer.getUser();
        WebSocketSession speechSession = context.getSpeechSession();
        if (speechSession == null) {
            logger.warn("Speech session for user {} not found", user.getUserId());
            return true;
        }
        if (user.getModel().getSpeech() == null || context.isMuted()) {
            return true;
        }
        AtomicInteger speechTokens = (AtomicInteger) context.get(HandlerContext.SPEECH_TOKENS);
        if (speechTokens == null) {
            context.put(HandlerContext.SPEECH_TOKENS, speechTokens = new AtomicInteger());
        }
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
            logger.info("Speech sentence1: {}", sentence);
            speechSession.sendMessage(new BinaryMessage(speech(user, sentence)));
        }
        if (answer.isDone()) {
            String remaining = sentenceTokenizer.getRemaining();
            if (StringUtils.hasText(remaining)) {
                logger.info("Speech sentence2: {}", remaining);
                speechSession.sendMessage(new BinaryMessage(speech(user, remaining)));
            }
            Usage usage = answer.getUsage();
            if (usage == null) {
                usage = Usage.builder().user(user).build();
                answer.setUsage(usage);
            }
            usage.setSpeechTokens(speechTokens.get());
            logger.info("Speech complete, cost {}", ChatUtils.serialize(usage));
        }
        return true;
    }

}
