package com.themoa.policysearch.policy.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "policy")
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(name = "source_policy_id", nullable = false, unique = true, length = 100)
    private String sourcePolicyId;
    @Column(name = "source_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PolicySource sourceType;
    @Column(name = "agency_name", nullable = false, length = 100)
    private String agencyName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyCategory category;
    @Column(length = 500)
    private String summary;
    @Column(name = "official_url", length = 500)
    private String officialUrl;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "is_always_open", nullable = false)
    private boolean alwaysOpen;
    @Column(name = "is_active", nullable = false)
    private boolean active;
    @Column(nullable = false, length = 50)
    private String status;
    @OneToOne(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PolicyCondition condition;
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PolicyRegion> regions = new LinkedHashSet<>();

    protected Policy() {
    }

    public Policy(String title, String sourcePolicyId, PolicySource sourceType, String agencyName, PolicyCategory category) {
        this.title = title;
        this.sourcePolicyId = sourcePolicyId;
        this.sourceType = sourceType;
        this.agencyName = agencyName;
        this.category = category;
        this.active = true;
        this.status = ApplicationStatus.NEEDS_CONFIRMATION.name();
    }

    public void updateBasic(String title, String agencyName, PolicyCategory category, String summary, String officialUrl,
                            LocalDate startDate, LocalDate dueDate, boolean alwaysOpen, ApplicationStatus status) {
        this.title = title;
        this.agencyName = agencyName;
        this.category = category;
        this.summary = summary;
        this.officialUrl = officialUrl;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.alwaysOpen = alwaysOpen;
        this.status = status.name();
        this.active = status != ApplicationStatus.INACTIVE;
    }

    public void replaceCondition(PolicyCondition condition) {
        this.condition = condition;
        condition.attachPolicy(this);
    }

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getSourcePolicyId() { return sourcePolicyId; }
    public PolicySource getSourceType() { return sourceType; }
    public String getAgencyName() { return agencyName; }
    public PolicyCategory getCategory() { return category; }
    public String getSummary() { return summary; }
    public String getOfficialUrl() { return officialUrl; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isAlwaysOpen() { return alwaysOpen; }
    public boolean isActive() { return active; }
    public String getStatus() { return status; }
    public PolicyCondition getCondition() { return condition; }
    public Set<PolicyRegion> getRegions() { return regions; }
}
