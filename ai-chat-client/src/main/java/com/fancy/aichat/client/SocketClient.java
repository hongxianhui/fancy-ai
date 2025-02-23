package com.fancy.aichat.client;

import jakarta.annotation.Resource;
import org.fancy.aichat.common.Answer;
import org.fancy.aichat.common.Question;
import org.fancy.aichat.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class SocketClient implements InitializingBean, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    @Resource
    private List<QuestionHandler> handlers;

    @Value("${ai.host}")
    private String aiHost;
    @Value("${ai.port}")
    private Integer aiPort;
    @Value("${ai.secrete}")
    private String aiSecret;
    @Value("${ai.keep-alive.internal}")
    private Integer aiKeepAliveInternal;

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket(aiHost, aiPort);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            writer.println(aiSecret);
            writer.flush();
            logger.info("Client: connected to {}:{}", aiHost, aiPort);
            keepAlive(socket);
            listenQuestion(socket);
        } catch (Exception ex) {
            logger.error("Client: connect to server failed. try re-connect.", ex);
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            run();
        }
    }

    private void keepAlive(Socket socket) {
        Utils.scheduleAtFixedRate(() -> {
            try {
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                writer.println(Utils.serialize(Answer.builder().type(Answer.TYPE_KEEPALIVE).build()));
                writer.flush();
                logger.info("Sent keep alive signal to {}", socket.getRemoteSocketAddress());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 10, aiKeepAliveInternal);
    }

    private void listenQuestion(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        loop:
        while (true) {
            String message = reader.readLine();
            Question question = Utils.deserialize(message, Question.class);
            logger.info("Question: {},", Utils.serialize(question));
            for (QuestionHandler handler : handlers) {
                if (handler.support(question)) {
                    try {
                        handler.handle(question, writer);
                    } catch (Exception e) {
                        logger.error("Client: handle question failed.", e);
                    }
                    continue loop;
                }
            }
            writer.println(Utils.serialize(Answer.builder().user(question.getUser()).type(Answer.TYPE_ANSWER).content("模型不存在！").done().build()));
            writer.flush();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Utils.schedule(this, 5);
    }

}
