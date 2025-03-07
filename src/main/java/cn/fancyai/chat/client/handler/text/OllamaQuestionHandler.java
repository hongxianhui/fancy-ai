package cn.fancyai.chat.client.handler.text;

import cn.fancyai.chat.client.ChatUtils;
import cn.fancyai.chat.client.handler.HandlerContext;
import cn.fancyai.chat.client.handler.QuestionHandler;
import cn.fancyai.chat.client.tools.ChatTool;
import cn.fancyai.chat.objects.Answer;
import cn.fancyai.chat.objects.Question;
import cn.fancyai.chat.objects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(510)
public class OllamaQuestionHandler implements QuestionHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ChatClient chatClient;

    public OllamaQuestionHandler(ChatMemory chatMemory) {
        //Embedding
        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .ollamaApi(new OllamaApi())
                .defaultOptions(OllamaOptions.builder().model("nomic-embed-text").build())
                .build();
        VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        //Tool
        List<FunctionCallback> functionCallbacks = ChatTool.generateFunctionCallbacks();
        //Options
        OllamaOptions defaultOptions = OllamaOptions.builder()
                .functionCallbacks(functionCallbacks)
                .build();
        //Model
        OllamaChatModel chatModel = OllamaChatModel.builder().ollamaApi(new OllamaApi()).defaultOptions(defaultOptions).build();
        //Client
        chatClient = ChatClient.builder(chatModel)
                .defaultSystem(ChatUtils.getPrompt("qwen2.5-1.5b-instruct-system.txt"))
                .defaultFunctions(functionCallbacks.toArray(new FunctionCallback[0]))
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(chatMemory).chatMemoryRetrieveSize(10).build()
                        , new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().build(), ChatUtils.getPrompt("rag.txt"))
                )
                .build();
    }

    protected String getModelName() {
        return "qwen2.5:0.5b";
    }

    @Override
    public boolean handle(Question question, HandlerContext context) throws IOException {
        User user = question.getUser();
        if (!getModelName().equals(user.getModel().getChat())) {
            return false;
        }
        logger.info("Handle question: {}::{}", getClass().getSimpleName(), getModelName());
        //Tools
        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put("question", question);
        toolContext.put("handlerContext", context);
        //Message
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(ChatUtils.getPrompt("qwen2.5-1.5b-instruct-system.txt")));
        messages.add(new UserMessage(question.getContent()));
        //Options
        OllamaOptions chatOptions = OllamaOptions.builder()
                .model(getModelName())
                .toolContext(toolContext)
                .build();
        //Call
        chatClient.prompt(new Prompt(messages, chatOptions)).stream().chatResponse().subscribe(chatResponse -> {
            try {
                String content = chatResponse.getResult().getOutput().getText();
                String toolName = chatResponse.getResult().getMetadata().get("toolName");
                Boolean done = chatResponse.getMetadata().get("done");
                Answer answer = Answer.builder(user)
                        .content(content)
                        .build();
                if (Boolean.TRUE.equals(done)) {
                    answer.setDone(true);
                }
                if (StringUtils.hasText(toolName)) {
                    answer = ChatUtils.deserialize(content, Answer.class);
                }
                user.getChatSession().sendMessage(answer, context);
            } catch (Exception e) {
                logger.error("Ollama call failed.", e);
            }
        });
        return true;
    }
}
