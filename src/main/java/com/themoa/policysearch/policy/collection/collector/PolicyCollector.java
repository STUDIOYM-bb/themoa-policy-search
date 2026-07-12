package com.themoa.policysearch.policy.collection.collector;

import com.themoa.policysearch.policy.collection.dto.PolicyCollectionPage;
import com.themoa.policysearch.policy.domain.PolicySource;

public interface PolicyCollector {
    PolicySource source();
    PolicyCollectionPage collectPage(int page, int size);
}
