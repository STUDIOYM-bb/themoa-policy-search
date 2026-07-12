package com.themoa.policysearch.policy.bookmark;

import com.themoa.policysearch.common.exception.NotFoundException;
import com.themoa.policysearch.member.domain.Member;
import com.themoa.policysearch.member.service.CurrentMemberProvider;
import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.domain.PolicyBookmark;
import com.themoa.policysearch.policy.domain.PolicyCalendarEvent;
import com.themoa.policysearch.policy.repository.PolicyBookmarkRepository;
import com.themoa.policysearch.policy.repository.PolicyCalendarEventRepository;
import com.themoa.policysearch.policy.repository.PolicyRepository;
import com.themoa.policysearch.policy.search.dto.PolicyResultItem;
import com.themoa.policysearch.policy.search.evaluator.PolicyApplicationStatusCalculator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyBookmarkService {
    private final CurrentMemberProvider currentMemberProvider;
    private final PolicyRepository policyRepository;
    private final PolicyBookmarkRepository bookmarkRepository;
    private final PolicyCalendarEventRepository calendarEventRepository;
    private final PolicyApplicationStatusCalculator statusCalculator;

    public PolicyBookmarkService(CurrentMemberProvider currentMemberProvider, PolicyRepository policyRepository,
                                 PolicyBookmarkRepository bookmarkRepository,
                                 PolicyCalendarEventRepository calendarEventRepository,
                                 PolicyApplicationStatusCalculator statusCalculator) {
        this.currentMemberProvider = currentMemberProvider;
        this.policyRepository = policyRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.calendarEventRepository = calendarEventRepository;
        this.statusCalculator = statusCalculator;
    }

    @Transactional
    public void add(Integer policyId) {
        Member member = currentMemberProvider.currentMember();
        Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new NotFoundException("정책을 찾을 수 없습니다."));
        if (!bookmarkRepository.existsByMemberIdAndPolicyId(member.getId(), policyId)) {
            bookmarkRepository.save(new PolicyBookmark(member, policy));
            createCalendarEvents(member, policy);
        }
    }

    @Transactional
    public void remove(Integer policyId) {
        Member member = currentMemberProvider.currentMember();
        bookmarkRepository.findByMemberIdAndPolicyId(member.getId(), policyId).ifPresent(bookmarkRepository::delete);
        calendarEventRepository.deleteByMemberIdAndPolicyId(member.getId(), policyId);
    }

    @Transactional(readOnly = true)
    public List<PolicyResultItem> list() {
        Member member = currentMemberProvider.currentMember();
        return bookmarkRepository.findByMemberIdOrderByIdDesc(member.getId()).stream()
                .map(bookmark -> {
                    Policy policy = bookmark.getPolicy();
                    return new PolicyResultItem(policy.getId(), policy.getTitle(), List.of(policy.getAgencyName()),
                            List.of(policy.getSourceType().name()),
                            policy.getRegions().stream().map(pr -> pr.getRegion().displayName()).toList(),
                            policy.getCategory().name(),
                            policy.getCondition() == null ? "확인 필요" : policy.getCondition().getConditionSummary(),
                            policy.getSummary(),
                            policy.isAlwaysOpen() ? "상시 신청" : policy.getStartDate() + " ~ " + policy.getDueDate(),
                            statusCalculator.calculate(policy),
                            com.themoa.policysearch.policy.domain.EligibilityStatus.NEEDS_CONFIRMATION,
                            List.of(), List.of(), List.of(),
                            "관심 정책으로 저장한 항목입니다.", policy.getOfficialUrl(), null, true);
                })
                .toList();
    }

    private void createCalendarEvents(Member member, Policy policy) {
        if (policy.isAlwaysOpen()) {
            return;
        }
        if (policy.getStartDate() != null) {
            calendarEventRepository.save(new PolicyCalendarEvent(member, policy, "APPLICATION_START", policy.getStartDate(), policy.getTitle() + " 신청 시작"));
        }
        if (policy.getDueDate() != null) {
            calendarEventRepository.save(new PolicyCalendarEvent(member, policy, "APPLICATION_END", policy.getDueDate(), policy.getTitle() + " 신청 마감"));
        }
    }
}
