package com.themoa.policysearch.policy.search.dto;

import com.themoa.policysearch.policy.domain.ApplicationStatus;
import com.themoa.policysearch.policy.domain.EligibilityStatus;
import java.util.List;

public record PolicyResultItem(
        Integer policyId,
        String policyName,
        List<String> organizations,
        List<String> sources,
        List<String> regionNames,
        String category,
        String targetSummary,
        String supportSummary,
        String applicationPeriod,
        ApplicationStatus applicationStatus,
        EligibilityStatus eligibilityStatus,
        List<String> matchedConditions,
        List<String> missingConditions,
        List<String> unmatchedConditions,
        String recommendationReason,
        String officialUrl,
        Double semanticScore,
        boolean bookmarked
) {
}
