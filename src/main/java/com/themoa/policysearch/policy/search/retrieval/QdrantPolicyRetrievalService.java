package com.themoa.policysearch.policy.search.retrieval;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QdrantPolicyRetrievalService {
    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final boolean ragEnabled;
    private final double minimumSimilarity;

    @Autowired
    public QdrantPolicyRetrievalService(ObjectProvider<VectorStore> vectorStoreProvider,
                                        @Value("${app.rag.enabled:false}") boolean ragEnabled,
                                        @Value("${app.rag.search.minimum-similarity:0.50}") double minimumSimilarity) {
        this.vectorStoreProvider = vectorStoreProvider;
        this.ragEnabled = ragEnabled;
        this.minimumSimilarity = minimumSimilarity;
    }

    QdrantPolicyRetrievalService(ObjectProvider<VectorStore> vectorStoreProvider, boolean ragEnabled) {
        this(vectorStoreProvider, ragEnabled, 0.50d);
    }

    public PolicyRetrievalResult retrieve(String query, int topK) {
        return retrieve(query, topK, minimumSimilarity);
    }

    public PolicyRetrievalResult retrieve(String query, int topK, double similarityThreshold) {
        Instant startedAt = Instant.now();
        if (!ragEnabled) {
            throw new IllegalStateException("RAG가 비활성화되어 있습니다.");
        }
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            throw new IllegalStateException("Qdrant VectorStore가 구성되지 않았습니다.");
        }
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();
        List<Document> documents = vectorStore.similaritySearch(request);
        List<PolicyRetrievalCandidate> candidates = new ArrayList<>();
        for (Document document : documents) {
            Integer policyId = policyId(document.getMetadata());
            if (policyId == null) {
                continue;
            }
            candidates.add(new PolicyRetrievalCandidate(
                    policyId,
                    document.getId(),
                    document.getScore(),
                    new LinkedHashMap<>(document.getMetadata()),
                    snippet(document.getText())
            ));
        }
        return PolicyRetrievalResult.rag(elapsedMillis(startedAt), candidates);
    }

    private Integer policyId(Map<String, Object> metadata) {
        Object value = metadata.get("policyId");
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String snippet(String text) {
        if (text == null) {
            return null;
        }
        String compact = text.replaceAll("\\s+", " ").trim();
        return compact.length() > 220 ? compact.substring(0, 220) + "..." : compact;
    }

    private long elapsedMillis(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).toMillis();
    }
}
