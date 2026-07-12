package com.themoa.policysearch.policy.search.retrieval;

import java.util.Map;

public record PolicyRetrievalCandidate(
        Integer policyId,
        String documentId,
        Double semanticScore,
        Map<String, Object> metadata,
        String contentSnippet
) {
}
