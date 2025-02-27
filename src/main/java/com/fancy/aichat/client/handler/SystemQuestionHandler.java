//package com.fancy.aichat.client.handler;
//
//import com.fancy.aichat.client.tools.GetUserInfoTool;
//import com.fancy.aichat.server.user.User;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import jakarta.annotation.Resource;
//import org.fancy.aichat.client.QuestionHandler;
//import org.fancy.aichat.common.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.ResourceUtils;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.reader.TextReader;
//import org.springframework.ai.vectorstore.SimpleVectorStore;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.TextMessage;
//
//import java.io.IOException;
//import java.util.List;
//
//@Component
//@Order(1)
//public class SystemQuestionHandler implements QuestionHandler {
//    private static final Logger logger = LoggerFactory.getLogger(SystemQuestionHandler.class);
//
//    @Resource
//    private GetUserInfoTool getUserInfoTool;
//
//    private final VectorStore vectorStore;
//
//    public SystemQuestionHandler(EmbeddingModel dashscopeEmbeddingModel) {
//        vectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
//    }
//
//    @Override
//    public void handle(Question question) throws IOException {
//        switch (question.getPrompt()) {
//            case USER_CONNECTED:
//                handleUserConnected(question);
//                break;
//            case USER_GONE:
//                handleUserGone(question);
//                break;
//            case ADD_KNOWLEDGE:
//                handleAddKnowledge(question);
//                break;
//            case SWITCH_MODEL:
//                handleSwitchModel(question);
//                break;
//            case SHOW_HELP:
//                handleShowHelp(question);
//        }
//    }
//
//    private void handleShowHelp(Question question) throws IOException {
//        sendAnswerMessage(question.getUser(), ResourceUtils.getText("classpath:prompt\help.html"));
//    }
//
//    private void handleSwitchModel(Question question) throws IOException {
//        String model = question.getContent().toLowerCase();
//        User user = question.getUser();
//        user.setModel(model);
//        sendAnswerMessage(user, "模型已切换至" + model + "。");
//    }
//
//    private void handleAddKnowledge(Question question) throws IOException {
//        vectorStore.add(List.of(new Document(question.getContent())));
//        String feedBackMessage = "好的，我记住了。";
//        sendAnswerMessage(question.getUser(), feedBackMessage);
//    }
//
//    private void handleUserGone(Question question) throws JsonProcessingException {
//        String content = question.getContent();
//        User[] users = Utils.deserialize(content, User[].class);
//        getUserInfoTool.setUsers(users);
//    }
//
//    private void handleUserConnected(Question question) throws IOException {
//        String content = question.getContent();
//        User[] users = Utils.deserialize(content, User[].class);
//        getUserInfoTool.setUsers(users);
//        sendAnswerMessage(question.getUser(), new TextReader("classpath:prompt\welcome.html").get().get(0).getText());
//    }
//
//    private void sendAnswerMessage(User user, String message) throws IOException {
//        Answer answer = Answer.builder().user(user).type(Answer.TYPE_ANSWER).content(message).done().build();
//        User chatUser = (User) user;
//        chatUser.getSession().sendMessage(new TextMessage(Utils.serialize(answer)));
//    }
//
//    @Override
//    public boolean support(Question question) {
//        determinePrompt(question);
//        return switch (question.getPrompt()) {
//            case SHOW_HELP, SWITCH_MODEL, USER_CONNECTED, USER_GONE, ADD_KNOWLEDGE -> true;
//            default -> false;
//        };
//    }
//
//    private void determinePrompt(Question question) {
//        String content = question.getContent().toLowerCase();
//        if (content.contains("切换") && content.contains("模型")) {
//            question.setPrompt(ChatPrompt.SWITCH_MODEL);
//            if (content.contains("deepseek满血版")) {
//                question.setContent("deepseek-r1");
//            }
//            if (content.contains("千问max")) {
//                question.setContent("qwen-max");
//            }
//        }
//    }
//}
