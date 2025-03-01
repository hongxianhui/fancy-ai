package com.fancy.aichat.client.tts;

import com.fancy.aichat.client.StreamTokenizer;

import java.util.Set;

public class ChatStreamTokenizer implements StreamTokenizer {

    private static final Set<Character> CN_PUNCTUATION = Set.of('。', '，', '！', '？');
    private final StringBuilder buffer = new StringBuilder();
    private final int minLength;

    public ChatStreamTokenizer(int minLength) {
        this.minLength = minLength;
    }

    @Override
    public String tokenize(String chunk) {
        StringBuilder output = new StringBuilder();
        String cleanChunk = chunk.replaceAll("\\s+", "");

        for (char c : cleanChunk.toCharArray()) {
            buffer.append(c);
            if (CN_PUNCTUATION.contains(c) && buffer.length() >= minLength) {
                output.append(buffer).append('\n');
                buffer.setLength(0);
            }
        }
        return output.toString().trim();
    }

    @Override
    public String getRemaining() {
        return buffer.toString().trim();
    }

}
