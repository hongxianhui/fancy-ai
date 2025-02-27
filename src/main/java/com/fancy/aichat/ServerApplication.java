package com.fancy.aichat;


import com.fancy.aichat.endpoint.WebSocketEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@SpringBootApplication
@EnableWebSocket
public class ServerApplication implements WebSocketConfigurer {
    public static ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketEndpoint(), "/ws").setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler webSocketEndpoint() {
        return new WebSocketEndpoint();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

}