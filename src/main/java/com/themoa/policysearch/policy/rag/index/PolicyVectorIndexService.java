package com.themoa.policysearch.policy.rag.index;

import com.themoa.policysearch.policy.rag.document.PolicyDocument;

public interface PolicyVectorIndexService {
    void upsert(PolicyDocument document);
    void delete(String documentId);
    boolean available();
}
