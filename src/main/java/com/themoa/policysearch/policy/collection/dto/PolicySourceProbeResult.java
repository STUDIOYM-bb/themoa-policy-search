package com.themoa.policysearch.policy.collection.dto;

import com.themoa.policysearch.policy.domain.PolicySource;

public record PolicySourceProbeResult(
        PolicySource source,
        boolean keyConfigured,
        boolean actuallyCalled,
        String maskedRequestUrl,
        Integer httpStatus,
        String contentType,
        int responseLength,
        String responseType,
        String responsePreview,
        boolean errorResponse,
        String errorCode,
        String errorMessage,
        boolean listNodeFound,
        Integer totalCount,
        int parsedCount,
        String firstPolicyId,
        String firstPolicyName
) {
}
