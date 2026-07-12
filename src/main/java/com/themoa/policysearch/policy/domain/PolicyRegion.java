package com.themoa.policysearch.policy.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "policy_region")
public class PolicyRegion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionCode region;

    protected PolicyRegion() {
    }

    public PolicyRegion(Policy policy, RegionCode region) {
        this.policy = policy;
        this.region = region;
    }

    public RegionCode getRegion() { return region; }
}
