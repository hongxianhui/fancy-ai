package com.fancy.aichat.client;

import com.fancy.aichat.common.Question;

import java.io.IOException;

public interface QuestionHandler {

    void handle(Question question) throws IOException, Exception;

    boolean support(Question question);
}
