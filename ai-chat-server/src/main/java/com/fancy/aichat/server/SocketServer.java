package com.fancy.aichat.server;

import com.fancy.aichat.server.user.ChatUser;
import com.fancy.aichat.server.user.UserManager;
import jakarta.annotation.Resource;
import org.fancy.aichat.common.Answer;
import org.fancy.aichat.common.Question;
import org.fancy.aichat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Component
public class SocketServer implements Runnable, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    @Value("${ai.msgFail}")
    private String aiMsgFail;
    @Value("${ai.secrete}")
    private String aiSecret;
    @Value("${ai.port}")
    private int aiPort;
    @Value("${ai.timeout}")
    private int aiTimeout;

    @Resource
    private UserManager userManager;
    @Resource
    private TTSServer ttsServer;

    private Socket socket;
    private ServerSocket serverSocket;

    public void run() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
            serverSocket = new ServerSocket(aiPort);
            logger.info("Proxy server started on port {}, waiting client connect", serverSocket.getLocalPort());
            Socket s = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String secrete = reader.readLine();
            if (!secrete.equals(aiSecret)) {
                logger.warn("Unauthorized client from {}", s.getInetAddress().getHostAddress());
                run();
                return;
            }
            socket = s;
            socket.setSoTimeout(aiTimeout);
            logger.info("Server: proxy client connected from {}", s.getInetAddress().getHostAddress());
            answer();
        } catch (IOException e) {
            logger.error("Server: connection failed, waiting client re-connect", e);
            run();
        }
    }

    private void answer() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                Answer answer = Utils.deserialize(line, Answer.class);
                if (Answer.TYPE_KEEPALIVE.equals(answer.getType())) {
                    logger.info("Got keepalive signal from client {}", socket.getInetAddress().getHostAddress());
                    continue;
                }
                ChatUser user = userManager.updateUser(answer.getUser());
                if (user == null) {
                    logger.warn("User session {} is closed.", answer.getUser().getUserId());
                    continue;
                }
                if (Answer.TYPE_SPEECH.equals(answer.getType())) {
                    ttsServer.transform(user.getUserId(), answer.getContent());
                    continue;
                }
                if (answer.isDone()) {
                    ttsServer.done(user.getUserId());
                    logger.info("Answer is completed for user {}", user.getUserId());
                }
                user.getSession().sendMessage(new TextMessage(line));
            } catch (IOException e) {
                logger.error("Server: connection failed, waiting client re-connect", e);
            }
        }
    }

    public void ask(Question question) throws IOException {
        ChatUser user = (ChatUser) question.getUser();
        if (socket == null || socket.isClosed()) {
            logger.warn("Client is not available.");
            Answer answer = Answer.builder().user(user).type(Answer.TYPE_ANSWER).content(aiMsgFail).build();
            user.getSession().sendMessage(new TextMessage(Utils.serialize(answer)));
            return;
        }
        try {
            String questionJson = Utils.serialize(question);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            writer.println(questionJson);
            writer.flush();
        } catch (IOException e) {
            logger.error("Connection to client failed.", e);
            Answer answer = Answer.builder().user(user).type(Answer.TYPE_ANSWER).content(aiMsgFail).build();
            user.getSession().sendMessage(new TextMessage(Utils.serialize(answer)));
            Utils.submit(this);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Utils.submit(this);
    }
}
