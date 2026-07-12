package com.themoa.policysearch.policy.search.dto;

import java.util.List;

public record PolicySearchResponse(
        String originalQuery,
        PolicySearchCondition interpretedConditions,
        List<String> missingConditions,
        boolean needsMoreInformation,
        List<String> followUpQuestions,
        String parserMode,
        boolean parserFallback,
        String parserFallbackReason,
        String searchMode,
        boolean ragAttempted,
        boolean ragSucceeded,
        boolean degraded,
        String fallbackReason,
        int vectorCandidateCount,
        int databaseCandidateCount,
        int filteredResultCount,
        long elapsedTimeMs,
        String message,
        List<PolicyResultItem> results,
        int page,
        int size,
        long totalElements,
        int regionFilteredCount,
        int targetFilteredCount,
        int similarityFilteredCount,
        int finalResultCount,
        boolean retriedWithLargerTopK,
        boolean mysqlFallbackUsed
) {
    public PolicySearchResponse(String originalQuery, PolicySearchCondition interpretedConditions,
                                List<String> missingConditions, boolean needsMoreInformation,
                                List<String> followUpQuestions, String parserMode, boolean parserFallback,
                                String parserFallbackReason, String searchMode, boolean ragAttempted,
                                boolean ragSucceeded, boolean degraded, String fallbackReason,
                                int vectorCandidateCount, int databaseCandidateCount, int filteredResultCount,
                                long elapsedTimeMs, String message, List<PolicyResultItem> results,
                                int page, int size, long totalElements) {
        this(originalQuery, interpretedConditions, missingConditions, needsMoreInformation, followUpQuestions,
                parserMode, parserFallback, parserFallbackReason, searchMode, ragAttempted, ragSucceeded,
                degraded, fallbackReason, vectorCandidateCount, databaseCandidateCount, filteredResultCount,
                elapsedTimeMs, message, results, page, size, totalElements, filteredResultCount, filteredResultCount,
                vectorCandidateCount, results == null ? 0 : results.size(), false, degraded);
    }
}
