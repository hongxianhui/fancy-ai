package com.fancy.aichat.manager;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("knowledgeManager")
public class KnowledgeManager {

    private final VectorStore vectorStore;

    public KnowledgeManager(EmbeddingModel dashscopeEmbeddingModel) {
        vectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
    }

    public void addKnowledge(String knowledge) {
        vectorStore.add(List.of(new Document(knowledge)));
    }
}
