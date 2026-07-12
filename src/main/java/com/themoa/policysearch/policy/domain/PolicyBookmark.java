package com.themoa.policysearch.policy.domain;

import com.themoa.policysearch.member.domain.Member;
import jakarta.persistence.*;

@Entity
@Table(name = "policy_bookmark")
public class PolicyBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    @Column(name = "apply_status", nullable = false, length = 50)
    private String applyStatus;
    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled;
    @Column(length = 500)
    private String note;

    protected PolicyBookmark() {
    }

    public PolicyBookmark(Member member, Policy policy) {
        this.member = member;
        this.policy = policy;
        this.applyStatus = "INTERESTED";
        this.notificationEnabled = true;
    }

    public Integer getId() { return id; }
    public Policy getPolicy() { return policy; }
    public String getApplyStatus() { return applyStatus; }
}
