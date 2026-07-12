package com.themoa.policysearch.policy.domain;

import com.themoa.policysearch.member.domain.Member;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "policy_calendar_event")
public class PolicyCalendarEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;
    @Column(nullable = false, length = 200)
    private String title;

    protected PolicyCalendarEvent() {
    }

    public PolicyCalendarEvent(Member member, Policy policy, String eventType, LocalDate eventDate, String title) {
        this.member = member;
        this.policy = policy;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.title = title;
    }

    public Integer getId() { return id; }
    public Policy getPolicy() { return policy; }
    public String getEventType() { return eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public String getTitle() { return title; }
}
