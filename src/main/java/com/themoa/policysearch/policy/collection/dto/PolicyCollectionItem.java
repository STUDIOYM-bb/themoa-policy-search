package com.themoa.policysearch.policy.collection.dto;

import com.themoa.policysearch.policy.domain.PolicyCategory;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PolicyCollectionItem(
        PolicySource source,
        String sourcePolicyId,
        String policyName,
        String organization,
        String managingOrganization,
        List<String> regionNames,
        List<String> regionCodes,
        PolicyCategory category,
        List<String> targetGroups,
        Integer minimumAge,
        Integer maximumAge,
        String employmentConditions,
        Boolean studentConditions,
        String incomeCondition,
        String housingCondition,
        String householdCondition,
        String selectionCriteria,
        String supportContent,
        String applicationMethod,
        String requiredDocuments,
        LocalDate applicationStartDate,
        LocalDate applicationEndDate,
        String applicationPeriodText,
        boolean alwaysOpen,
        String officialUrl,
        String contact,
        List<String> keywords,
        LocalDateTime sourceUpdatedAt,
        LocalDateTime collectedAt,
        Long rawDataId
) {
}
