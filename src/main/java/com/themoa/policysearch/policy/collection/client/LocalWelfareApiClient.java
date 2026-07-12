package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LocalWelfareApiClient extends SourceEndpointSupport {
    private final PolicyCollectionProperties properties;

    public LocalWelfareApiClient(ExternalPolicyApiClient apiClient, PolicyCollectionProperties properties) {
        super(apiClient);
        this.properties = properties;
    }

    public ExternalApiResponse fetchList(int page, int size) {
        var props = properties.getLocalWelfare();
        return getXml(PolicySource.LOCAL_WELFARE, props, props.getListPath(), pageParams(props.getApiKey(), page, size));
    }

    public ExternalApiResponse fetchDetail(String serviceId) {
        var props = properties.getLocalWelfare();
        requireText(PolicySource.LOCAL_WELFARE, props.getDetailPath(), "detail-path");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("serviceKey", props.getApiKey());
        params.put("servId", serviceId);
        return getXml(PolicySource.LOCAL_WELFARE, props, props.getDetailPath(), params);
    }

    public String listRequestUrl(int page, int size) {
        var props = properties.getLocalWelfare();
        return apiClient.requestUrl(props.getBaseUrl(), props.getListPath(), pageParams(props.getApiKey(), page, size));
    }

    public String detailRequestUrl(String serviceId) {
        var props = properties.getLocalWelfare();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("serviceKey", props.getApiKey());
        params.put("servId", serviceId);
        return apiClient.requestUrl(props.getBaseUrl(), props.getDetailPath(), params);
    }

    private Map<String, Object> pageParams(String apiKey, int page, int size) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("serviceKey", apiKey);
        params.put("pageNo", page);
        params.put("numOfRows", size);
        return params;
    }
}
