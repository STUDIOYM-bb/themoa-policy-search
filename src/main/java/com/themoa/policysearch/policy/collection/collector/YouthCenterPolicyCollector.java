package com.themoa.policysearch.policy.collection.collector;

import com.themoa.policysearch.policy.collection.client.ExternalApiResponse;
import com.themoa.policysearch.policy.collection.client.YouthCenterApiClient;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionPage;
import com.themoa.policysearch.policy.collection.mapper.ParsedPolicyPage;
import com.themoa.policysearch.policy.collection.mapper.YouthCenterPolicyMapper;
import com.themoa.policysearch.policy.domain.PolicySource;
import org.springframework.stereotype.Component;

@Component
public class YouthCenterPolicyCollector implements PolicyCollector {
    private final YouthCenterApiClient apiClient;
    private final YouthCenterPolicyMapper mapper;

    public YouthCenterPolicyCollector(YouthCenterApiClient apiClient, YouthCenterPolicyMapper mapper) {
        this.apiClient = apiClient;
        this.mapper = mapper;
    }

    @Override
    public PolicySource source() {
        return PolicySource.YOUTH_CENTER;
    }

    @Override
    public PolicyCollectionPage collectPage(int page, int size) {
        ExternalApiResponse response = apiClient.list(page, size);
        ParsedPolicyPage parsed = mapper.parse(page, response);
        boolean hasNext = parsed.totalCount() > 0 ? page * size < parsed.totalCount() : !parsed.items().isEmpty();
        return new PolicyCollectionPage(parsed.items(), page, 1, parsed.totalCount(), hasNext,
                response.body(), response.maskedRequestUrl(), response.statusCode(), response.contentType(), "JSON");
    }
}
