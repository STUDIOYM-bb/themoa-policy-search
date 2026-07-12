package com.themoa.policysearch.policy.repository;

import com.themoa.policysearch.policy.domain.PolicyBookmark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyBookmarkRepository extends JpaRepository<PolicyBookmark, Integer> {
    boolean existsByMemberIdAndPolicyId(Integer memberId, Integer policyId);
    Optional<PolicyBookmark> findByMemberIdAndPolicyId(Integer memberId, Integer policyId);
    @EntityGraph(attributePaths = {"policy", "policy.condition", "policy.regions", "policy.regions.region"})
    List<PolicyBookmark> findByMemberIdOrderByIdDesc(Integer memberId);
}
