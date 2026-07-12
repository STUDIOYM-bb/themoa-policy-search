package com.themoa.policysearch.policy.repository;

import com.themoa.policysearch.policy.domain.PolicyCollectionRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyCollectionRunRepository extends JpaRepository<PolicyCollectionRun, Long> {
    List<PolicyCollectionRun> findTop20ByOrderByStartedAtDesc();
}
