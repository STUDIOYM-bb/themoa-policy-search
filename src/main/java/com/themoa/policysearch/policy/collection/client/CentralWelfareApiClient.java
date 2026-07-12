package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CentralWelfareApiClient extends SourceEndpointSupport {
    private final PolicyCollectionProperties properties;

    public CentralWelfareApiClient(ExternalPolicyApiClient apiClient, PolicyCollectionProperties properties) {
        super(apiClient);
        this.properties = properties;
    }

    public ExternalApiResponse fetchList(int page, int size) {
        var props = properties.getCentralWelfare();
        return getXml(PolicySource.CENTRAL_WELFARE, props, props.getListPath(), pageParams(props.getApiKey(), page, size));
    }

    public ExternalApiResponse fetchDetail(String serviceId) {
        var props = properties.getCentralWelfare();
        requireText(PolicySource.CENTRAL_WELFARE, props.getDetailPath(), "detail-path");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("serviceKey", props.getApiKey());
        params.put("servId", serviceId);
        params.put("callTp", "D");
        return getXml(PolicySource.CENTRAL_WELFARE, props, props.getDetailPath(), params);
    }

    public String listRequestUrl(int page, int size) {
        var props = properties.getCentralWelfare();
        return apiClient.requestUrl(props.getBaseUrl(), props.getListPath(), pageParams(props.getApiKey(), page, size));
    }

    public String detailRequestUrl(String serviceId) {
        var props = properties.getCentralWelfare();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("serviceKey", props.getApiKey());
        params.put("servId", serviceId);
        params.put("callTp", "D");
        return apiClient.requestUrl(props.getBaseUrl(), props.getDetailPath(), params);
    }

    private Map<String, Object> pageParams(String apiKey, int page, int size) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("serviceKey", apiKey);
        params.put("callTp", "L");
        params.put("pageNo", page);
        params.put("numOfRows", size);
        params.put("srchKeyCode", "001");
        return params;
    }
}
