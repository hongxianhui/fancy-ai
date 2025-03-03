package cn.fancyai.chat.client.tokenizer;

public interface StreamTokenizer {

    String tokenize(String chunk);

    String getRemaining();
}
