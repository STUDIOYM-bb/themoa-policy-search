package com.themoa.policysearch.policy.rag.index;

import com.themoa.policysearch.policy.rag.document.PolicyDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.rag", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopPolicyVectorIndexService implements PolicyVectorIndexService {
    @Override
    public void upsert(PolicyDocument document) {
    }

    @Override
    public void delete(String documentId) {
    }

    @Override
    public boolean available() {
        return false;
    }
}
