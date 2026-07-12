package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.common.config.PolicyCollectionProperties.SourceProperties;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.Map;

abstract class SourceEndpointSupport {
    protected final ExternalPolicyApiClient apiClient;

    protected SourceEndpointSupport(ExternalPolicyApiClient apiClient) {
        this.apiClient = apiClient;
    }

    protected ExternalApiResponse getJson(PolicySource source, SourceProperties props, String path, Map<String, ?> params) {
        validate(source, props, path);
        return apiClient.getJson(source, props.getBaseUrl(), path, params);
    }

    protected ExternalApiResponse getXml(PolicySource source, SourceProperties props, String path, Map<String, ?> params) {
        validate(source, props, path);
        return apiClient.getXml(source, props.getBaseUrl(), path, params);
    }

    protected void validate(PolicySource source, SourceProperties props, String path) {
        requireText(source, props.getBaseUrl(), "base-url");
        requireText(source, path, "endpoint path");
        requireText(source, props.getApiKey(), "API key");
    }

    protected void requireText(PolicySource source, String value, String name) {
        if (value == null || value.isBlank()) {
            throw new PolicyApiException("해당 출처의 " + name + " 설정이 비어 있습니다. source=" + source);
        }
    }
}
