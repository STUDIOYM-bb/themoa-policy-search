package com.themoa.policysearch.policy.collection.dto;

import java.util.List;

public record PolicyCollectionPage(
        List<PolicyCollectionItem> items,
        int page,
        int apiRequestCount,
        int totalCount,
        boolean hasNext,
        String rawBody,
        String requestUrl
) {
}
