package com.themoa.policysearch.policy.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy_embedding_sync")
public class PolicyEmbeddingSync {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false, unique = true)
    private Policy policy;
    @Column(name = "content_hash", length = 64)
    private String contentHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 30)
    private EmbeddingSyncStatus syncStatus;
    @Column(name = "last_error", length = 1000)
    private String lastError;
    @Column(name = "retry_count", nullable = false)
    private int retryCount;
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    protected PolicyEmbeddingSync() {
    }

    public PolicyEmbeddingSync(Policy policy) {
        this.policy = policy;
        this.syncStatus = EmbeddingSyncStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    public void markPending(String hash) {
        this.contentHash = hash;
        this.syncStatus = EmbeddingSyncStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    public void markSynced(String hash) {
        this.contentHash = hash;
        this.syncStatus = EmbeddingSyncStatus.SYNCED;
        this.syncedAt = LocalDateTime.now();
        this.lastError = null;
    }

    public void markFailed(String message) {
        this.syncStatus = EmbeddingSyncStatus.FAILED;
        this.lastError = message;
        this.retryCount++;
    }

    public Policy getPolicy() { return policy; }
    public String getContentHash() { return contentHash; }
    public EmbeddingSyncStatus getSyncStatus() { return syncStatus; }
}
