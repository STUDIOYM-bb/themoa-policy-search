package com.themoa.policysearch.policy.collection.dto;

import com.themoa.policysearch.policy.domain.PolicySource;

public record PolicyCollectionResult(
        PolicySource source,
        String status,
        int requestedPageCount,
        int apiRequestCount,
        int listRequestCount,
        int detailRequestCount,
        int supportConditionRequestCount,
        int failedRequestCount,
        int receivedCount,
        int processedCount,
        int insertedCount,
        int updatedCount,
        int skippedCount,
        int failedCount,
        int itemFailedCount,
        int pageFailedCount,
        String representativeError
) {
    public PolicyCollectionResult(PolicySource source, int requestedPageCount, int apiRequestCount,
                                  int receivedCount, int insertedCount, int updatedCount, int skippedCount,
                                  int failedCount, String representativeError) {
        this(source, "SUCCESS", requestedPageCount, apiRequestCount, requestedPageCount, Math.max(0, apiRequestCount - requestedPageCount),
                0, failedCount, receivedCount, insertedCount + updatedCount + skippedCount, insertedCount, updatedCount,
                skippedCount, failedCount, failedCount, failedCount, representativeError);
    }

    public static PolicyCollectionResult empty(PolicySource source) {
        return new PolicyCollectionResult(source, "SKIPPED", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null);
    }

    public static PolicyCollectionResult skipped(PolicySource source, String reason) {
        return new PolicyCollectionResult(source, "SKIPPED", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, reason);
    }
}
