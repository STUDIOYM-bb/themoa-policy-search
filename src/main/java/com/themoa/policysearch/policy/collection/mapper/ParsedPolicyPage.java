package com.themoa.policysearch.policy.collection.mapper;

import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import java.util.List;

public record ParsedPolicyPage(
        List<PolicyCollectionItem> items,
        boolean listNodeFound,
        int totalCount,
        String firstPolicyId,
        String firstPolicyName
) {
}
