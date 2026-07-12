package com.themoa.policysearch.policy.rag.index;

import com.themoa.policysearch.policy.rag.document.PolicyDocument;
import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.rag", name = "enabled", havingValue = "true")
public class SpringAiPolicyVectorIndexService implements PolicyVectorIndexService {
    private final ObjectProvider<VectorStore> vectorStoreProvider;

    public SpringAiPolicyVectorIndexService(ObjectProvider<VectorStore> vectorStoreProvider) {
        this.vectorStoreProvider = vectorStoreProvider;
    }

    @Override
    public void upsert(PolicyDocument document) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            throw new IllegalStateException("Qdrant VectorStore가 구성되지 않았습니다.");
        }
        vectorStore.add(List.of(new Document(document.id(), document.content(), document.metadata())));
    }

    @Override
    public void delete(String documentId) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore != null) {
            vectorStore.delete(List.of(documentId));
        }
    }

    @Override
    public boolean available() {
        return vectorStoreProvider.getIfAvailable() != null;
    }
}
