package com.themoa.policysearch.policy.collection.collector;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.collection.client.ExternalApiResponse;
import com.themoa.policysearch.policy.collection.client.GovServiceApiClient;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionPage;
import com.themoa.policysearch.policy.collection.dto.govservice.*;
import com.themoa.policysearch.policy.collection.mapper.GovServicePolicyMapper;
import com.themoa.policysearch.policy.collection.mapper.ParsedPolicyPage;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GovServiceInfoCollector implements PolicyCollector {
    private final GovServiceApiClient apiClient;
    private final GovServicePolicyMapper mapper;
    private final PolicyCollectionProperties properties;

    public GovServiceInfoCollector(GovServiceApiClient apiClient, GovServicePolicyMapper mapper,
                                   PolicyCollectionProperties properties) {
        this.apiClient = apiClient;
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    public PolicySource source() {
        return PolicySource.GOV_SERVICE;
    }

    @Override
    public PolicyCollectionPage collectPage(int page, int size) {
        ExternalApiResponse response = apiClient.fetchServiceList(page, size);
        GovServiceListResponse listResponse = mapper.parseListResponse(page, response);
        ParsedPolicyPage parsedPage = mapper.toPage(listResponse);
        DetailResult detailResult = enrichDetails(listResponse.data());
        boolean hasNext = parsedPage.totalCount() > 0 ? page * size < parsedPage.totalCount() : !detailResult.items().isEmpty();
        return new PolicyCollectionPage(detailResult.items(), page, 1 + detailResult.detailRequestCount() + detailResult.supportRequestCount(),
                1, detailResult.detailRequestCount(), detailResult.supportRequestCount(), detailResult.failedRequestCount(),
                parsedPage.totalCount(), hasNext, response.body(), response.maskedRequestUrl(),
                response.statusCode(), response.contentType(), "JSON");
    }

    private DetailResult enrichDetails(List<GovServiceListItem> listItems) {
        List<PolicyCollectionItem> items = new ArrayList<>();
        int detailRequestCount = 0;
        int supportRequestCount = 0;
        int failedRequestCount = 0;
        for (GovServiceListItem listItem : listItems == null ? List.<GovServiceListItem>of() : listItems) {
            GovServiceDetailItem detail = null;
            GovServiceSupportConditionItem supportCondition = null;
            if (listItem.serviceId() != null && !listItem.serviceId().isBlank()) {
                try {
                    ExternalApiResponse detailResponse = apiClient.fetchServiceDetail(listItem.serviceId());
                    detailRequestCount++;
                    detail = mapper.firstDetail(mapper.parseDetailResponse(listItem.serviceId(), detailResponse));
                } catch (RuntimeException ignored) {
                    failedRequestCount++;
                    detail = null;
                }
                sleepDetailDelay();
                try {
                    ExternalApiResponse supportResponse = apiClient.fetchSupportConditions(listItem.serviceId());
                    supportRequestCount++;
                    supportCondition = mapper.firstSupportCondition(mapper.parseSupportConditionsResponse(listItem.serviceId(), supportResponse));
                } catch (RuntimeException ignored) {
                    failedRequestCount++;
                    supportCondition = null;
                }
                sleepDetailDelay();
            }
            PolicyCollectionItem item = mapper.toItem(listItem, detail, supportCondition);
            if (item.policyName() != null && !item.policyName().isBlank()) {
                items.add(item);
            }
        }
        return new DetailResult(items, detailRequestCount, supportRequestCount, failedRequestCount);
    }

    private void sleepDetailDelay() {
        try {
            Thread.sleep(properties.getDetailRequestDelay().toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("상세조회 대기가 중단되었습니다.", ex);
        }
    }

    private record DetailResult(List<PolicyCollectionItem> items, int detailRequestCount, int supportRequestCount,
                                int failedRequestCount) {
    }
}
