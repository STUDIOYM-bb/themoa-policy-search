package com.themoa.policysearch.policy.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy_collection_run")
public class PolicyCollectionRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PolicySource source;
    @Column(name = "execution_type", nullable = false, length = 30)
    private String executionType;
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CollectionStatus status;
    @Column(name = "requested_page_count", nullable = false)
    private int requestedPageCount;
    @Column(name = "api_request_count", nullable = false)
    private int apiRequestCount;
    @Column(name = "received_count", nullable = false)
    private int receivedCount;
    @Column(name = "inserted_count", nullable = false)
    private int insertedCount;
    @Column(name = "updated_count", nullable = false)
    private int updatedCount;
    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;
    @Column(name = "failed_count", nullable = false)
    private int failedCount;
    @Column(name = "representative_error", length = 1000)
    private String representativeError;
    @Column(name = "failed_page")
    private Integer failedPage;
    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    protected PolicyCollectionRun() {
    }

    public PolicyCollectionRun(PolicySource source, String executionType) {
        this.source = source;
        this.executionType = executionType;
        this.startedAt = LocalDateTime.now();
        this.status = CollectionStatus.RUNNING;
    }

    public void complete(CollectionStatus status, String representativeError) {
        this.status = status;
        this.representativeError = representativeError;
        this.completedAt = LocalDateTime.now();
    }

    public void addStats(int pages, int requests, int received, int inserted, int updated, int skipped, int failed) {
        this.requestedPageCount += pages;
        this.apiRequestCount += requests;
        this.receivedCount += received;
        this.insertedCount += inserted;
        this.updatedCount += updated;
        this.skippedCount += skipped;
        this.failedCount += failed;
    }

    public Long getId() { return id; }
    public PolicySource getSource() { return source; }
    public String getExecutionType() { return executionType; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public CollectionStatus getStatus() { return status; }
    public int getRequestedPageCount() { return requestedPageCount; }
    public int getApiRequestCount() { return apiRequestCount; }
    public int getReceivedCount() { return receivedCount; }
    public int getInsertedCount() { return insertedCount; }
    public int getUpdatedCount() { return updatedCount; }
    public int getSkippedCount() { return skippedCount; }
    public int getFailedCount() { return failedCount; }
    public String getRepresentativeError() { return representativeError; }
    public Integer getFailedPage() { return failedPage; }
    public int getRetryCount() { return retryCount; }
}
