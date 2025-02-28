package com.fancy.aichat.client;

import com.fancy.aichat.objects.Question;

public interface QuestionHandler {

    boolean handle(Question question) throws Exception;

}
