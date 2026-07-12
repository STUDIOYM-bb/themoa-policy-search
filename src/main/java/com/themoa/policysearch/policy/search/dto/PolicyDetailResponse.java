package com.themoa.policysearch.policy.search.dto;

import java.util.List;

public record PolicyDetailResponse(
        Integer policyId,
        String policyName,
        String organization,
        List<String> sources,
        List<String> regionNames,
        String category,
        String supportContent,
        String targetSummary,
        String applicationPeriod,
        String applicationStatus,
        String applicationMethod,
        String requiredDocuments,
        String contact,
        String officialUrl,
        String notice
) {
}
