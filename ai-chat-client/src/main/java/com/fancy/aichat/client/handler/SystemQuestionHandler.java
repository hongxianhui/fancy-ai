package com.fancy.aichat.client.handler;

import com.fancy.aichat.client.QuestionHandler;
import com.fancy.aichat.tools.ContextTool;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.fancy.aichat.common.Answer;
import org.fancy.aichat.common.Question;
import org.fancy.aichat.common.User;
import org.fancy.aichat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ResourceUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Component
@Order(1)
public class SystemQuestionHandler implements QuestionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SystemQuestionHandler.class);

    @Resource
    private VectorStore vectorStore;
    @Resource
    private ContextTool contextTool;

    @Override
    public void handle(Question question, PrintWriter writer) throws IOException {
        switch (question.getPrompt()) {
            case USER_CONNECTED:
                handleUserConnected(question, writer);
                break;
            case USER_GONE:
                handleUserGone(question);
                break;
            case ADD_KNOWLEDGE:
                handleAddKnowledge(question, writer);
                break;
            case SWITCH_MODEL:
                handleSwitchModel(question, writer);
                break;
            case SHOW_HELP:
                handleShowHelp(question, writer);
        }
    }

    private void handleShowHelp(Question question, PrintWriter writer) throws JsonProcessingException {
        sendAnswerMessage(writer, question.getUser(), ResourceUtils.getText("classpath:help.html"));
    }

    private void handleSwitchModel(Question question, PrintWriter writer) throws JsonProcessingException {
        String content = question.getContent().toLowerCase();
        if (content.contains("千问") || content.contains("qwen")) {
            User user = question.getUser();
            user.setModel("qwen2.5:1.5b");
            sendAnswerMessage(writer, user, "大语言模型已切换至qwen2.5:1.5b。");
            return;
        }
        if (content.contains("深度求索") || content.contains("deepseek")) {
            User user = question.getUser();
            user.setModel("deepseek-r1:1.5b");
            sendAnswerMessage(writer, user, "大语言模型已切换至deepseek-r1:1.5b。");
            return;
        }
        sendAnswerMessage(writer, question.getUser(), "模型不存在。");
    }

    private void handleAddKnowledge(Question question, PrintWriter writer) throws JsonProcessingException {
        vectorStore.add(List.of(new Document(question.getContent())));
        String feedBackMessage = "好的，我记住了。";
        sendAnswerMessage(writer, question.getUser(), feedBackMessage);
    }

    private void handleUserGone(Question question) throws JsonProcessingException {
        String content = question.getContent();
        User[] users = Utils.deserialize(content, User[].class);
        contextTool.setUsers(users);
    }

    private void handleUserConnected(Question question, PrintWriter writer) throws JsonProcessingException {
        String content = question.getContent();
        User[] users = Utils.deserialize(content, User[].class);
        contextTool.setUsers(users);
        sendAnswerMessage(writer, question.getUser(), new TextReader("classpath:welcome.html").get().get(0).getText());
    }

    private void sendAnswerMessage(PrintWriter writer, User user, String message) throws JsonProcessingException {
        Answer answer = Answer.builder().user(user).type(Answer.TYPE_ANSWER).content(message).done().build();
        writer.println(Utils.serialize(answer));
        writer.flush();
    }

    @Override
    public boolean support(Question question) {
        if (question.getPrompt() == null) {
            return false;
        }
        return switch (question.getPrompt()) {
            case SHOW_HELP, SWITCH_MODEL, USER_CONNECTED, USER_GONE, ADD_KNOWLEDGE -> true;
            default -> false;
        };
    }
}
