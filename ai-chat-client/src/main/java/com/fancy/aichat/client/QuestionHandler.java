package com.fancy.aichat.client;

import org.fancy.aichat.common.Question;

import java.io.IOException;
import java.io.PrintWriter;

public interface QuestionHandler {

    void handle(Question question, PrintWriter writer) throws IOException;

    boolean support(Question question);
}
