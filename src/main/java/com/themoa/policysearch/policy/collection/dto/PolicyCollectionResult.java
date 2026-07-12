package com.themoa.policysearch.policy.collection.dto;

public record PolicyCollectionResult(
        int requestedPageCount,
        int apiRequestCount,
        int receivedCount,
        int insertedCount,
        int updatedCount,
        int skippedCount,
        int failedCount,
        String representativeError
) {
    public static PolicyCollectionResult empty() {
        return new PolicyCollectionResult(0, 0, 0, 0, 0, 0, 0, null);
    }
}
