package com.themoa.policysearch.policy.repository;

import com.themoa.policysearch.policy.domain.PolicyCalendarEvent;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyCalendarEventRepository extends JpaRepository<PolicyCalendarEvent, Integer> {
    void deleteByMemberIdAndPolicyId(Integer memberId, Integer policyId);
    @EntityGraph(attributePaths = {"policy"})
    List<PolicyCalendarEvent> findByMemberIdAndEventDateBetweenOrderByEventDateAsc(Integer memberId, LocalDate from, LocalDate to);
}
