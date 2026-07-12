package com.themoa.policysearch.policy.calendar;

import com.themoa.policysearch.member.domain.Member;
import com.themoa.policysearch.member.service.CurrentMemberProvider;
import com.themoa.policysearch.policy.domain.PolicyCalendarEvent;
import com.themoa.policysearch.policy.repository.PolicyCalendarEventRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyCalendarService {
    private final CurrentMemberProvider currentMemberProvider;
    private final PolicyCalendarEventRepository repository;

    public PolicyCalendarService(CurrentMemberProvider currentMemberProvider, PolicyCalendarEventRepository repository) {
        this.currentMemberProvider = currentMemberProvider;
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PolicyCalendarEventResponse> events(LocalDate from, LocalDate to) {
        Member member = currentMemberProvider.currentMember();
        return repository.findByMemberIdAndEventDateBetweenOrderByEventDateAsc(member.getId(), from, to)
                .stream()
                .map(PolicyCalendarEventResponse::from)
                .toList();
    }
}
