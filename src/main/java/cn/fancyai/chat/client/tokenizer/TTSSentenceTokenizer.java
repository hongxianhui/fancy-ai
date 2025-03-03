package cn.fancyai.chat.client.tokenizer;

import java.util.Set;

public class TTSSentenceTokenizer implements StreamTokenizer {

    private static final Set<Character> CN_PUNCTUATION = Set.of('。', '，', '！', '？');
    private final StringBuilder buffer = new StringBuilder();
    private final int minLength;

    public TTSSentenceTokenizer(int minLength) {
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
