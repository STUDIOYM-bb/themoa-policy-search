package com.themoa.policysearch.policy.dev;

import com.themoa.policysearch.policy.domain.EmbeddingSyncStatus;
import com.themoa.policysearch.policy.repository.PolicyCollectionRunRepository;
import com.themoa.policysearch.policy.repository.PolicyEmbeddingSyncRepository;
import com.themoa.policysearch.policy.repository.PolicyRepository;
import java.util.List;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DevStatusService {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final PolicyRepository policyRepository;
    private final PolicyEmbeddingSyncRepository embeddingSyncRepository;
    private final PolicyCollectionRunRepository collectionRunRepository;
    private final String qdrantHost;
    private final int qdrantRestPort;
    private final String qdrantApiKey;
    private final String collectionName;
    private final boolean ragEnabled;
    private final String openAiApiKey;

    public DevStatusService(JdbcTemplate jdbcTemplate,
                            ObjectProvider<VectorStore> vectorStoreProvider,
                            PolicyRepository policyRepository,
                            PolicyEmbeddingSyncRepository embeddingSyncRepository,
                            PolicyCollectionRunRepository collectionRunRepository,
                            @Value("${QDRANT_HOST:localhost}") String qdrantHost,
                            @Value("${QDRANT_REST_PORT:6333}") int qdrantRestPort,
                            @Value("${QDRANT_API_KEY:}") String qdrantApiKey,
                            @Value("${app.rag.collection-name:policies}") String collectionName,
                            @Value("${app.rag.enabled:false}") boolean ragEnabled,
                            @Value("${OPENAI_API_KEY:}") String openAiApiKey) {
        this.jdbcTemplate = jdbcTemplate;
        this.vectorStoreProvider = vectorStoreProvider;
        this.policyRepository = policyRepository;
        this.embeddingSyncRepository = embeddingSyncRepository;
        this.collectionRunRepository = collectionRunRepository;
        this.qdrantHost = qdrantHost;
        this.qdrantRestPort = qdrantRestPort;
        this.qdrantApiKey = qdrantApiKey;
        this.collectionName = collectionName;
        this.ragEnabled = ragEnabled;
        this.openAiApiKey = openAiApiKey;
    }

    public DevStatusResponse status() {
        return new DevStatusResponse(
                "UP",
                databaseStatus(),
                qdrantStatus(),
                openAiApiKey != null && !openAiApiKey.isBlank(),
                ragEnabled,
                vectorStoreProvider.getIfAvailable() != null,
                collectionName,
                policyRepository.count(),
                policyRepository.countByActiveTrue(),
                new EmbeddingSummary(
                        embeddingSyncRepository.countBySyncStatus(EmbeddingSyncStatus.PENDING),
                        embeddingSyncRepository.countBySyncStatus(EmbeddingSyncStatus.SYNCED),
                        embeddingSyncRepository.countBySyncStatus(EmbeddingSyncStatus.FAILED)
                )
        );
    }

    public List<CollectionRunItem> recentRuns(int limit) {
        return collectionRunRepository.findTop20ByOrderByStartedAtDesc().stream()
                .limit(Math.max(1, Math.min(limit, 20)))
                .map(run -> new CollectionRunItem(run.getId(), run.getSource().name(), run.getExecutionType(),
                        run.getStartedAt() == null ? null : run.getStartedAt().toString(),
                        run.getCompletedAt() == null ? null : run.getCompletedAt().toString(),
                        run.getStatus().name(), run.getReceivedCount(), run.getInsertedCount(),
                        run.getUpdatedCount(), run.getSkippedCount(), run.getFailedCount(),
                        run.getRepresentativeError()))
                .toList();
    }

    public EmbeddingSummary embeddingSummary() {
        return status().embedding();
    }

    private String databaseStatus() {
        try {
            jdbcTemplate.queryForObject("select 1", Integer.class);
            return "UP";
        } catch (RuntimeException ex) {
            return "DOWN";
        }
    }

    private String qdrantStatus() {
        try {
            RestClient.Builder builder = RestClient.builder().baseUrl("http://" + qdrantHost + ":" + qdrantRestPort);
            if (qdrantApiKey != null && !qdrantApiKey.isBlank()) {
                builder.defaultHeader("api-key", qdrantApiKey);
            }
            builder.build().get().uri("/collections").retrieve().toBodilessEntity();
            return "UP";
        } catch (RuntimeException ex) {
            return "DOWN";
        }
    }

    public record DevStatusResponse(String application, String database, String qdrant,
                                    boolean openAiConfigured, boolean ragEnabled, boolean vectorStoreAvailable,
                                    String collectionName, long policyCount, long activePolicyCount,
                                    EmbeddingSummary embedding) {
    }

    public record EmbeddingSummary(long pending, long synced, long failed) {
    }

    public record CollectionRunItem(Long id, String source, String executionType, String startedAt, String completedAt,
                                    String status, int receivedCount, int insertedCount, int updatedCount,
                                    int skippedCount, int failedCount, String representativeError) {
    }
}
