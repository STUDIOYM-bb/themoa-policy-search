package com.themoa.policysearch.policy.rag.index;

import com.themoa.policysearch.policy.domain.EmbeddingSyncStatus;
import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.domain.PolicyEmbeddingSync;
import com.themoa.policysearch.policy.rag.document.PolicyDocument;
import com.themoa.policysearch.policy.rag.document.PolicyDocumentBuilder;
import com.themoa.policysearch.policy.repository.PolicyEmbeddingSyncRepository;
import com.themoa.policysearch.policy.repository.PolicyRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PolicyEmbeddingSyncService {
    private final PolicyRepository policyRepository;
    private final PolicyEmbeddingSyncRepository syncRepository;
    private final PolicyDocumentBuilder documentBuilder;
    private final PolicyVectorIndexService vectorIndexService;
    private final int batchSize;
    private final int maxBatchesPerRun;
    private final Duration requestDelay;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public PolicyEmbeddingSyncService(PolicyRepository policyRepository, PolicyEmbeddingSyncRepository syncRepository,
                                      PolicyDocumentBuilder documentBuilder, PolicyVectorIndexService vectorIndexService,
                                      @Value("${app.rag.embedding.batch-size:100}") int batchSize,
                                      @Value("${app.rag.embedding.max-batches-per-run:1000}") int maxBatchesPerRun,
                                      @Value("${app.rag.embedding.request-delay:50ms}") Duration requestDelay,
                                      TransactionTemplate transactionTemplate) {
        this.policyRepository = policyRepository;
        this.syncRepository = syncRepository;
        this.documentBuilder = documentBuilder;
        this.vectorIndexService = vectorIndexService;
        this.batchSize = Math.max(1, batchSize);
        this.maxBatchesPerRun = Math.max(1, maxBatchesPerRun);
        this.requestDelay = requestDelay == null ? Duration.ZERO : requestDelay;
        this.transactionTemplate = transactionTemplate;
    }

    PolicyEmbeddingSyncService(PolicyRepository policyRepository, PolicyEmbeddingSyncRepository syncRepository,
                               PolicyDocumentBuilder documentBuilder, PolicyVectorIndexService vectorIndexService,
                               int batchSize, int maxBatchesPerRun, Duration requestDelay) {
        this.policyRepository = policyRepository;
        this.syncRepository = syncRepository;
        this.documentBuilder = documentBuilder;
        this.vectorIndexService = vectorIndexService;
        this.batchSize = Math.max(1, batchSize);
        this.maxBatchesPerRun = Math.max(1, maxBatchesPerRun);
        this.requestDelay = requestDelay == null ? Duration.ZERO : requestDelay;
        this.transactionTemplate = null;
    }

    public EmbeddingQueueResult enqueueAll() {
        List<Policy> policies = policyRepository.findAllByActiveTrue();
        int newlyQueued = 0;
        int requeued = 0;
        int unchanged = 0;
        int failed = 0;
        String representativeError = null;
        for (Policy policy : policies) {
            try {
                PolicyDocument document = documentBuilder.build(policy);
                QueueOutcome outcome = queuePolicy(policy, document.contentHash());
                newlyQueued += outcome.newlyQueued() ? 1 : 0;
                requeued += outcome.requeued() ? 1 : 0;
                unchanged += outcome.unchanged() ? 1 : 0;
            } catch (RuntimeException ex) {
                failed++;
                if (representativeError == null) {
                    representativeError = "policyId=" + policy.getId() + ": " + safeMessage(ex);
                }
            }
        }
        return new EmbeddingQueueResult(policies.size(), newlyQueued, requeued, unchanged, 0,
                syncRepository.countBySyncStatus(EmbeddingSyncStatus.PENDING), failed, representativeError);
    }

    private QueueOutcome queuePolicy(Policy policy, String contentHash) {
        if (transactionTemplate == null) {
            return queuePolicyInTransaction(policy, contentHash);
        }
        return transactionTemplate.execute(status -> queuePolicyInTransaction(policy, contentHash));
    }

    private QueueOutcome queuePolicyInTransaction(Policy policy, String contentHash) {
        PolicyEmbeddingSync sync = syncRepository.findByPolicyId(policy.getId()).orElse(null);
        if (sync == null) {
            PolicyEmbeddingSync created = new PolicyEmbeddingSync(policy);
            created.markPending(contentHash);
            syncRepository.save(created);
            return QueueOutcome.queued();
        }
        if (contentHash.equals(sync.getContentHash()) && sync.getSyncStatus() == EmbeddingSyncStatus.SYNCED) {
            return QueueOutcome.unchangedOutcome();
        }
        sync.markPending(contentHash);
        syncRepository.save(sync);
        return QueueOutcome.requeuedOutcome();
    }

    @Transactional
    public EmbeddingProcessResult processPending() {
        return process(EmbeddingSyncStatus.PENDING);
    }

    @Transactional
    public EmbeddingProcessResult retryFailed() {
        return process(EmbeddingSyncStatus.FAILED);
    }

    private EmbeddingProcessResult process(EmbeddingSyncStatus status) {
        Instant startedAt = Instant.now();
        long totalTarget = syncRepository.countBySyncStatus(status);
        int processed = 0;
        int success = 0;
        int failed = 0;
        int currentBatch = 0;
        while (currentBatch < maxBatchesPerRun) {
            List<PolicyEmbeddingSync> targets = syncRepository.findBySyncStatusOrderByRequestedAtAsc(
                    status, PageRequest.of(0, batchSize));
            if (targets.isEmpty()) {
                break;
            }
            currentBatch++;
            for (PolicyEmbeddingSync sync : targets) {
                processed++;
                try {
                    PolicyDocument document = documentBuilder.build(sync.getPolicy());
                    vectorIndexService.upsert(document);
                    sync.markSynced(document.contentHash());
                    success++;
                } catch (RuntimeException ex) {
                    sync.markFailed(safeMessage(ex));
                    failed++;
                }
                sleepDelay();
            }
        }
        return new EmbeddingProcessResult(totalTarget, processed, success, failed,
                syncRepository.countBySyncStatus(status), currentBatch,
                Duration.between(startedAt, Instant.now()).toMillis());
    }

    private void sleepDelay() {
        if (requestDelay.isZero() || requestDelay.isNegative()) {
            return;
        }
        try {
            Thread.sleep(requestDelay.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("임베딩 처리 대기가 중단되었습니다.", ex);
        }
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }

    public record EmbeddingQueueResult(long activePolicyCount, int newlyQueuedCount, int requeuedCount,
                                       int unchangedCount, int deactivatedCount, long pendingCountAfter,
                                       int failedCount, String representativeError) {
    }

    private record QueueOutcome(boolean newlyQueued, boolean requeued, boolean unchanged) {
        static QueueOutcome queued() { return new QueueOutcome(true, false, false); }
        static QueueOutcome requeuedOutcome() { return new QueueOutcome(false, true, false); }
        static QueueOutcome unchangedOutcome() { return new QueueOutcome(false, false, true); }
    }

    public record EmbeddingProcessResult(long totalTargetCount, int processedCount, int successCount, int failedCount,
                                         long remainingPendingCount, int currentBatch, long elapsedTimeMs) {
    }
}
