package com.themoa.policysearch.policy.repository;

import com.themoa.policysearch.policy.domain.PolicyRawData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRawDataRepository extends JpaRepository<PolicyRawData, Long> {
}
