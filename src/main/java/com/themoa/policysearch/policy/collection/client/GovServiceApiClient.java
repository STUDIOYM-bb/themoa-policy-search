package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GovServiceApiClient extends SourceEndpointSupport {
    private static final String SERVICE_ID_CONDITION = "cond[서비스ID::EQ]";
    private final PolicyCollectionProperties properties;

    public GovServiceApiClient(ExternalPolicyApiClient apiClient, PolicyCollectionProperties properties) {
        super(apiClient);
        this.properties = properties;
    }

    public ExternalApiResponse fetchServiceList(int page, int size) {
        var props = properties.getGovService();
        Map<String, Object> params = baseParams(props.getApiKey());
        params.put("page", page);
        params.put("perPage", size);
        return getJson(PolicySource.GOV_SERVICE, props, props.getListPath(), params);
    }

    public ExternalApiResponse fetchServiceDetail(String serviceId) {
        var props = properties.getGovService();
        requireText(PolicySource.GOV_SERVICE, props.getDetailPath(), "detail-path");
        Map<String, Object> params = baseParams(props.getApiKey());
        params.put("page", 1);
        params.put("perPage", 1);
        params.put(SERVICE_ID_CONDITION, serviceId);
        return getJson(PolicySource.GOV_SERVICE, props, props.getDetailPath(), params);
    }

    public ExternalApiResponse fetchSupportConditions(String serviceId) {
        var props = properties.getGovService();
        requireText(PolicySource.GOV_SERVICE, props.getSupportConditionsPath(), "support-conditions-path");
        Map<String, Object> params = baseParams(props.getApiKey());
        params.put("page", 1);
        params.put("perPage", 1);
        params.put(SERVICE_ID_CONDITION, serviceId);
        return getJson(PolicySource.GOV_SERVICE, props, props.getSupportConditionsPath(), params);
    }

    public String listRequestUrl(int page, int size) {
        var props = properties.getGovService();
        Map<String, Object> params = baseParams(props.getApiKey());
        params.put("page", page);
        params.put("perPage", size);
        return apiClient.requestUrl(props.getBaseUrl(), props.getListPath(), params);
    }

    public String detailRequestUrl(String serviceId) {
        var props = properties.getGovService();
        Map<String, Object> params = baseParams(props.getApiKey());
        params.put("page", 1);
        params.put("perPage", 1);
        params.put(SERVICE_ID_CONDITION, serviceId);
        return apiClient.requestUrl(props.getBaseUrl(), props.getDetailPath(), params);
    }

    public String supportConditionsRequestUrl(String serviceId) {
        var props = properties.getGovService();
        Map<String, Object> params = baseParams(props.getApiKey());
        params.put("page", 1);
        params.put("perPage", 1);
        params.put(SERVICE_ID_CONDITION, serviceId);
        return apiClient.requestUrl(props.getBaseUrl(), props.getSupportConditionsPath(), params);
    }

    private Map<String, Object> baseParams(String apiKey) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("serviceKey", apiKey);
        params.put("returnType", "JSON");
        return params;
    }
}
