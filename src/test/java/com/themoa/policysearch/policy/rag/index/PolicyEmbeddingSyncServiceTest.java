package com.themoa.policysearch.policy.rag.index;

import com.themoa.policysearch.policy.domain.EmbeddingSyncStatus;
import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.domain.PolicyCategory;
import com.themoa.policysearch.policy.domain.PolicyEmbeddingSync;
import com.themoa.policysearch.policy.domain.PolicySource;
import com.themoa.policysearch.policy.rag.document.PolicyDocumentBuilder;
import com.themoa.policysearch.policy.repository.PolicyEmbeddingSyncRepository;
import com.themoa.policysearch.policy.repository.PolicyRepository;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PolicyEmbeddingSyncServiceTest {
    @Test
    void processesPendingUntilNoPendingRemainsAcrossBatches() {
        PolicyRepository policyRepository = mock(PolicyRepository.class);
        PolicyEmbeddingSyncRepository syncRepository = mock(PolicyEmbeddingSyncRepository.class);
        PolicyVectorIndexService vectorIndexService = mock(PolicyVectorIndexService.class);
        PolicyEmbeddingSync first = sync(1);
        PolicyEmbeddingSync second = sync(2);
        PolicyEmbeddingSync third = sync(3);
        when(syncRepository.countBySyncStatus(EmbeddingSyncStatus.PENDING)).thenReturn(3L, 0L);
        when(syncRepository.findBySyncStatusOrderByRequestedAtAsc(eq(EmbeddingSyncStatus.PENDING), any(Pageable.class)))
                .thenReturn(List.of(first, second), List.of(third), List.of());

        PolicyEmbeddingSyncService service = new PolicyEmbeddingSyncService(policyRepository, syncRepository,
                new PolicyDocumentBuilder(), vectorIndexService, 2, 10, Duration.ZERO);

        PolicyEmbeddingSyncService.EmbeddingProcessResult result = service.processPending();

        assertThat(result.totalTargetCount()).isEqualTo(3);
        assertThat(result.processedCount()).isEqualTo(3);
        assertThat(result.successCount()).isEqualTo(3);
        assertThat(result.currentBatch()).isEqualTo(2);
        verify(vectorIndexService, times(3)).upsert(any());
    }

    @Test
    void enqueueAllSkipsUnchangedSyncedPolicies() {
        PolicyRepository policyRepository = mock(PolicyRepository.class);
        PolicyEmbeddingSyncRepository syncRepository = mock(PolicyEmbeddingSyncRepository.class);
        PolicyVectorIndexService vectorIndexService = mock(PolicyVectorIndexService.class);
        Policy policy = policy(1);
        PolicyEmbeddingSync sync = new PolicyEmbeddingSync(policy);
        String hash = new PolicyDocumentBuilder().build(policy).contentHash();
        sync.markSynced(hash);
        when(policyRepository.findAllByActiveTrue()).thenReturn(List.of(policy));
        when(syncRepository.findByPolicyId(1)).thenReturn(java.util.Optional.of(sync));
        when(syncRepository.countBySyncStatus(EmbeddingSyncStatus.PENDING)).thenReturn(0L);

        PolicyEmbeddingSyncService service = new PolicyEmbeddingSyncService(policyRepository, syncRepository,
                new PolicyDocumentBuilder(), vectorIndexService, 2, 10, Duration.ZERO);

        PolicyEmbeddingSyncService.EmbeddingQueueResult result = service.enqueueAll();

        assertThat(result.activePolicyCount()).isEqualTo(1);
        assertThat(result.unchangedCount()).isEqualTo(1);
        assertThat(result.newlyQueuedCount()).isZero();
        assertThat(result.requeuedCount()).isZero();
    }

    private PolicyEmbeddingSync sync(int id) {
        return new PolicyEmbeddingSync(policy(id));
    }

    private Policy policy(int id) {
        Policy policy = new Policy("정책 " + id, "SRC-" + id, PolicySource.GOV_SERVICE, "기관", PolicyCategory.생활지원);
        ReflectionTestUtils.setField(policy, "id", id);
        return policy;
    }
}
