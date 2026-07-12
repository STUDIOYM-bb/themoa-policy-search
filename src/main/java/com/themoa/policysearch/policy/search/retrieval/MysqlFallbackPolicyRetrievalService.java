package com.themoa.policysearch.policy.search.retrieval;

import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.repository.PolicyRepository;
import com.themoa.policysearch.policy.search.dto.PolicySearchCondition;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class MysqlFallbackPolicyRetrievalService {
    private final PolicyRepository policyRepository;

    public MysqlFallbackPolicyRetrievalService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public PolicyRetrievalResult retrieve(String query, PolicySearchCondition condition, int limit, String reason) {
        Instant startedAt = Instant.now();
        Page<Policy> page = policyRepository.fallbackSearch(keyword(condition, query), PageRequest.of(0, limit));
        List<PolicyRetrievalCandidate> candidates = page.getContent().stream()
                .map(policy -> new PolicyRetrievalCandidate(policy.getId(), null, null, java.util.Map.of(), null))
                .toList();
        return PolicyRetrievalResult.fallback(reason, (int) page.getTotalElements(),
                Duration.between(startedAt, Instant.now()).toMillis(), candidates);
    }

    private String keyword(PolicySearchCondition condition, String query) {
        if (condition.getCategory() != null && !condition.getCategory().isBlank()) {
            return condition.getCategory();
        }
        if (condition.getKeywords() != null && !condition.getKeywords().isEmpty()) {
            return condition.getKeywords().get(0);
        }
        return query == null || query.length() <= 20 ? query : query.substring(0, 20);
    }
}
