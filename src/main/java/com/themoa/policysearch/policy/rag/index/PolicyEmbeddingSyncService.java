package com.themoa.policysearch.policy.rag.index;

import com.themoa.policysearch.policy.domain.EmbeddingSyncStatus;
import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.domain.PolicyEmbeddingSync;
import com.themoa.policysearch.policy.rag.document.PolicyDocument;
import com.themoa.policysearch.policy.rag.document.PolicyDocumentBuilder;
import com.themoa.policysearch.policy.repository.PolicyEmbeddingSyncRepository;
import com.themoa.policysearch.policy.repository.PolicyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyEmbeddingSyncService {
    private final PolicyRepository policyRepository;
    private final PolicyEmbeddingSyncRepository syncRepository;
    private final PolicyDocumentBuilder documentBuilder;
    private final PolicyVectorIndexService vectorIndexService;

    public PolicyEmbeddingSyncService(PolicyRepository policyRepository, PolicyEmbeddingSyncRepository syncRepository,
                                      PolicyDocumentBuilder documentBuilder, PolicyVectorIndexService vectorIndexService) {
        this.policyRepository = policyRepository;
        this.syncRepository = syncRepository;
        this.documentBuilder = documentBuilder;
        this.vectorIndexService = vectorIndexService;
    }

    @Transactional
    public int enqueueAll() {
        List<Policy> policies = policyRepository.findAll();
        for (Policy policy : policies) {
            syncRepository.findByPolicyId(policy.getId())
                    .orElseGet(() -> syncRepository.save(new PolicyEmbeddingSync(policy)))
                    .markPending(null);
        }
        return policies.size();
    }

    @Transactional
    public int processPending() {
        return process(EmbeddingSyncStatus.PENDING);
    }

    @Transactional
    public int retryFailed() {
        return process(EmbeddingSyncStatus.FAILED);
    }

    private int process(EmbeddingSyncStatus status) {
        List<PolicyEmbeddingSync> targets = syncRepository.findTop100BySyncStatusOrderByRequestedAtAsc(status);
        int count = 0;
        for (PolicyEmbeddingSync sync : targets) {
            PolicyDocument document = documentBuilder.build(sync.getPolicy());
            if (document.contentHash().equals(sync.getContentHash()) && sync.getSyncStatus() == EmbeddingSyncStatus.SYNCED) {
                continue;
            }
            try {
                vectorIndexService.upsert(document);
                sync.markSynced(document.contentHash());
                count++;
            } catch (RuntimeException ex) {
                sync.markFailed(ex.getMessage());
            }
        }
        return count;
    }
}
