package com.themoa.policysearch.policy.collection.dto;

import java.util.List;

public record PolicyCollectionPage(
        List<PolicyCollectionItem> items,
        int page,
        int apiRequestCount,
        int listRequestCount,
        int detailRequestCount,
        int supportConditionRequestCount,
        int failedRequestCount,
        int totalCount,
        boolean hasNext,
        String rawBody,
        String requestUrl,
        int statusCode,
        String responseContentType,
        String responseFormat
) {
    public PolicyCollectionPage(List<PolicyCollectionItem> items, int page, int apiRequestCount, int totalCount,
                                boolean hasNext, String rawBody, String requestUrl, int statusCode,
                                String responseContentType, String responseFormat) {
        this(items, page, apiRequestCount, 1, Math.max(0, apiRequestCount - 1), 0, 0, totalCount, hasNext,
                rawBody, requestUrl, statusCode, responseContentType, responseFormat);
    }
}
