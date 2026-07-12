package com.themoa.policysearch.policy.repository;

import com.themoa.policysearch.policy.domain.EmbeddingSyncStatus;
import com.themoa.policysearch.policy.domain.PolicyEmbeddingSync;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyEmbeddingSyncRepository extends JpaRepository<PolicyEmbeddingSync, Long> {
    Optional<PolicyEmbeddingSync> findByPolicyId(Integer policyId);
    long countBySyncStatus(EmbeddingSyncStatus status);

    @EntityGraph(attributePaths = {"policy", "policy.condition", "policy.regions", "policy.regions.region"})
    List<PolicyEmbeddingSync> findBySyncStatusOrderByRequestedAtAsc(EmbeddingSyncStatus status, Pageable pageable);
}
