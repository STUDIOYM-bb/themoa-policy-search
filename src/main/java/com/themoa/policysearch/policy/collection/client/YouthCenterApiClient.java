package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class YouthCenterApiClient extends SourceEndpointSupport {
    private final PolicyCollectionProperties properties;

    public YouthCenterApiClient(ExternalPolicyApiClient apiClient, PolicyCollectionProperties properties) {
        super(apiClient);
        this.properties = properties;
    }

    public ExternalApiResponse list(int page, int size) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("openApiVlak", properties.getYouthCenter().getApiKey());
        params.put("pageIndex", page);
        params.put("display", size);
        return getJson(PolicySource.YOUTH_CENTER, properties.getYouthCenter(), properties.getYouthCenter().getListPath(), params);
    }
}
