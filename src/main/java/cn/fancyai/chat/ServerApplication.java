package cn.fancyai.chat;


import cn.fancyai.chat.endpoint.ChatEndpoint;
import cn.fancyai.chat.endpoint.SpeechEndpoint;
import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAutoConfiguration;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@EnableWebSocket
@SpringBootApplication(exclude = DashScopeAutoConfiguration.class)
public class ServerApplication implements WebSocketConfigurer {
    public static ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebsocket(), "/chat").setAllowedOrigins("*");
        registry.addHandler(speechWebsocket(), "/speech").setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 在此处设置bufferSize
        container.setMaxTextMessageBufferSize(512000);
        container.setMaxBinaryMessageBufferSize(512000);
        container.setMaxSessionIdleTimeout(15 * 60000L);
        return container;
    }

    @Bean
    public WebSocketHandler chatWebsocket() {
        return new ChatEndpoint();
    }

    @Bean
    public WebSocketHandler speechWebsocket() {
        return new SpeechEndpoint();
    }

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}

