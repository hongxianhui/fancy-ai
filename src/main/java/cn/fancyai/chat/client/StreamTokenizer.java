package cn.fancyai.chat.client;

public interface StreamTokenizer {

    String tokenize(String chunk);

    String getRemaining();
}
