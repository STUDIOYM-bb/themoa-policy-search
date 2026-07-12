package com.themoa.policysearch.policy.collection.collector;

import com.fasterxml.jackson.databind.JsonNode;
import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.collection.client.ExternalPolicyApiClient;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionPage;
import com.themoa.policysearch.policy.collection.normalizer.*;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LocalWelfareServiceCollector extends AbstractTreePolicyCollector {
    public LocalWelfareServiceCollector(ExternalPolicyApiClient apiClient, PolicyCollectionProperties properties,
                                        RegionNormalizer regionNormalizer, CategoryNormalizer categoryNormalizer,
                                        DateRangeNormalizer dateRangeNormalizer, AgeConditionNormalizer ageConditionNormalizer,
                                        KeywordNormalizer keywordNormalizer) {
        super(apiClient, properties, regionNormalizer, categoryNormalizer, dateRangeNormalizer, ageConditionNormalizer, keywordNormalizer);
    }

    @Override
    public PolicySource source() { return PolicySource.LOCAL_WELFARE; }

    @Override
    public PolicyCollectionPage collectPage(int page, int size) {
        String path = "/B554287/LocalGovernmentWelfareInformations/LcgvWelfarelist";
        Map<String, Object> params = Map.of("serviceKey", properties.getLocalWelfare().getApiKey(), "pageNo", page, "numOfRows", size);
        String raw = apiClient.get(properties.getLocalWelfare().getBaseUrl(), path, params);
        return parsePage(page, size, raw, apiClient.requestUrl(properties.getLocalWelfare().getBaseUrl(), path, params), true);
    }

    @Override
    protected PolicyCollectionItem toItem(JsonNode node) {
        return commonItem(node, source());
    }
}
