package com.fancy.aichat.tokenizer;

import java.util.Set;

public class ChatStreamTokenizer {
    private static final Set<Character> CN_PUNCTUATION = Set.of('。', '，', '！', '？');
    private final StringBuilder buffer = new StringBuilder();

    public String tokenize(String chunk) {
        StringBuilder output = new StringBuilder();
        String cleanChunk = chunk.replaceAll("\\s+", "");

        for (char c : cleanChunk.toCharArray()) {
            buffer.append(c);
            if (CN_PUNCTUATION.contains(c)) {
                output.append(buffer).append('\n');
                buffer.setLength(0);
            }
        }
        return output.toString().trim();
    }

    public String flushRemaining() {
        String remaining = buffer.toString();
        buffer.setLength(0);
        return remaining;
    }

}
