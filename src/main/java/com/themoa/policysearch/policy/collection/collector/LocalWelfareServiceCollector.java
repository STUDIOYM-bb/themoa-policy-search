package com.themoa.policysearch.policy.collection.collector;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.collection.client.ExternalApiResponse;
import com.themoa.policysearch.policy.collection.client.LocalWelfareApiClient;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionPage;
import com.themoa.policysearch.policy.collection.dto.localwelfare.*;
import com.themoa.policysearch.policy.collection.mapper.LocalWelfarePolicyMapper;
import com.themoa.policysearch.policy.collection.mapper.ParsedPolicyPage;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LocalWelfareServiceCollector implements PolicyCollector {
    private final LocalWelfareApiClient apiClient;
    private final LocalWelfarePolicyMapper mapper;
    private final PolicyCollectionProperties properties;

    public LocalWelfareServiceCollector(LocalWelfareApiClient apiClient, LocalWelfarePolicyMapper mapper,
                                        PolicyCollectionProperties properties) {
        this.apiClient = apiClient;
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    public PolicySource source() {
        return PolicySource.LOCAL_WELFARE;
    }

    @Override
    public PolicyCollectionPage collectPage(int page, int size) {
        ExternalApiResponse response = apiClient.fetchList(page, size);
        LocalWelfareListResponse listResponse = mapper.parseListResponse(page, response);
        ParsedPolicyPage parsedPage = mapper.toPage(listResponse);
        DetailResult detailResult = enrichDetails(mapper.listItems(listResponse));
        boolean hasNext = parsedPage.totalCount() > 0 ? page * size < parsedPage.totalCount() : !detailResult.items().isEmpty();
        return new PolicyCollectionPage(detailResult.items(), page, 1 + detailResult.requestCount(),
                1, detailResult.requestCount(), 0, detailResult.failedRequestCount(),
                parsedPage.totalCount(), hasNext, response.body(), response.maskedRequestUrl(),
                response.statusCode(), response.contentType(), "XML");
    }

    private DetailResult enrichDetails(List<LocalWelfareListItem> listItems) {
        List<PolicyCollectionItem> items = new ArrayList<>();
        int requestCount = 0;
        int failedRequestCount = 0;
        for (LocalWelfareListItem listItem : listItems) {
            LocalWelfareDetailItem detail = null;
            if (listItem.serviceId() != null && !listItem.serviceId().isBlank()) {
                try {
                    ExternalApiResponse detailResponse = apiClient.fetchDetail(listItem.serviceId());
                    requestCount++;
                    detail = mapper.firstDetail(mapper.parseDetailResponse(listItem.serviceId(), detailResponse));
                } catch (RuntimeException ignored) {
                    failedRequestCount++;
                    detail = null;
                }
                sleepDetailDelay();
            }
            PolicyCollectionItem item = mapper.toItem(listItem, detail);
            if (item.policyName() != null && !item.policyName().isBlank()) {
                items.add(item);
            }
        }
        return new DetailResult(items, requestCount, failedRequestCount);
    }

    private void sleepDetailDelay() {
        try {
            Thread.sleep(properties.getDetailRequestDelay().toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("상세조회 대기가 중단되었습니다.", ex);
        }
    }

    private record DetailResult(List<PolicyCollectionItem> items, int requestCount, int failedRequestCount) {
    }
}
