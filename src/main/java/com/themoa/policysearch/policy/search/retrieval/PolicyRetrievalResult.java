package com.themoa.policysearch.policy.search.retrieval;

import java.util.List;

public record PolicyRetrievalResult(
        PolicySearchMode searchMode,
        boolean ragAttempted,
        boolean ragSucceeded,
        boolean fallback,
        String fallbackReason,
        int vectorCandidateCount,
        int databaseCandidateCount,
        long elapsedTimeMs,
        List<PolicyRetrievalCandidate> candidates
) {
    public static PolicyRetrievalResult rag(long elapsedTimeMs, List<PolicyRetrievalCandidate> candidates) {
        return new PolicyRetrievalResult(PolicySearchMode.RAG, true, true, false, null,
                candidates.size(), 0, elapsedTimeMs, candidates);
    }

    public static PolicyRetrievalResult fallback(String reason, int databaseCandidateCount, long elapsedTimeMs,
                                                 List<PolicyRetrievalCandidate> candidates) {
        return new PolicyRetrievalResult(PolicySearchMode.MYSQL_FALLBACK, false, false, true, reason,
                0, databaseCandidateCount, elapsedTimeMs, candidates);
    }

    public PolicyRetrievalResult withMysqlFallback(String reason, int databaseCandidateCount,
                                                   List<PolicyRetrievalCandidate> mergedCandidates) {
        return new PolicyRetrievalResult(PolicySearchMode.RAG_WITH_MYSQL_FALLBACK, ragAttempted, ragSucceeded, true,
                reason, vectorCandidateCount, databaseCandidateCount, elapsedTimeMs, mergedCandidates);
    }

    public PolicyRetrievalResult withRagAttempt(boolean ragSucceeded, int vectorCandidateCount) {
        return new PolicyRetrievalResult(searchMode, true, ragSucceeded, fallback, fallbackReason,
                vectorCandidateCount, databaseCandidateCount, elapsedTimeMs, candidates);
    }
}
