package com.themoa.policysearch.policy.collection.collector;

import com.fasterxml.jackson.databind.JsonNode;
import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.collection.client.ExternalPolicyApiClient;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionPage;
import com.themoa.policysearch.policy.collection.normalizer.*;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class YouthCenterPolicyCollector extends AbstractTreePolicyCollector {
    public YouthCenterPolicyCollector(ExternalPolicyApiClient apiClient, PolicyCollectionProperties properties,
                                      RegionNormalizer regionNormalizer, CategoryNormalizer categoryNormalizer,
                                      DateRangeNormalizer dateRangeNormalizer, AgeConditionNormalizer ageConditionNormalizer,
                                      KeywordNormalizer keywordNormalizer) {
        super(apiClient, properties, regionNormalizer, categoryNormalizer, dateRangeNormalizer, ageConditionNormalizer, keywordNormalizer);
    }

    @Override
    public PolicySource source() { return PolicySource.YOUTH_CENTER; }

    @Override
    public PolicyCollectionPage collectPage(int page, int size) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("openApiVlak", properties.getYouthCenter().getApiKey());
        params.put("pageIndex", page);
        params.put("display", size);
        String raw = apiClient.get(properties.getYouthCenter().getBaseUrl(), "/opi/youthPlcyList.do", params);
        return parsePage(page, size, raw, apiClient.requestUrl(properties.getYouthCenter().getBaseUrl(), "/opi/youthPlcyList.do", params), false);
    }

    @Override
    protected PolicyCollectionItem toItem(JsonNode node) {
        return commonItem(node, source());
    }
}
