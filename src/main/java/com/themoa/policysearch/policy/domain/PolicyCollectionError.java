package com.themoa.policysearch.policy.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy_collection_error")
public class PolicyCollectionError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_run_id")
    private PolicyCollectionRun collectionRun;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_data_id")
    private PolicyRawData rawData;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PolicySource source;
    @Column(name = "failed_page")
    private Integer failedPage;
    @Column(name = "source_policy_id", length = 150)
    private String sourcePolicyId;
    @Column(name = "error_type", nullable = false, length = 100)
    private String errorType;
    @Column(name = "error_message", nullable = false, length = 1000)
    private String errorMessage;
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected PolicyCollectionError() {
    }

    public PolicyCollectionError(PolicyCollectionRun collectionRun, PolicySource source, Integer failedPage,
                                 String sourcePolicyId, String errorType, String errorMessage) {
        this.collectionRun = collectionRun;
        this.source = source;
        this.failedPage = failedPage;
        this.sourcePolicyId = sourcePolicyId;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.occurredAt = LocalDateTime.now();
    }
}
