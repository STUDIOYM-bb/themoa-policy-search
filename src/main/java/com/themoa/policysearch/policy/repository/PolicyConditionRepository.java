package com.themoa.policysearch.policy.repository;

import com.themoa.policysearch.policy.domain.PolicyCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyConditionRepository extends JpaRepository<PolicyCondition, Integer> {
}
