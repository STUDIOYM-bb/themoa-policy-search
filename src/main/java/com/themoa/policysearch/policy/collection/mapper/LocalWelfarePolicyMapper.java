package com.themoa.policysearch.policy.collection.mapper;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.themoa.policysearch.policy.collection.client.ExternalApiResponse;
import com.themoa.policysearch.policy.collection.client.PolicyApiParseException;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.dto.localwelfare.*;
import com.themoa.policysearch.policy.collection.normalizer.*;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LocalWelfarePolicyMapper extends PolicyMappingSupport {
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ExternalApiErrorInspector errorInspector;

    public LocalWelfarePolicyMapper(RegionNormalizer regionNormalizer, CategoryNormalizer categoryNormalizer,
                                    DateRangeNormalizer dateRangeNormalizer, AgeConditionNormalizer ageConditionNormalizer,
                                    KeywordNormalizer keywordNormalizer, ExternalApiErrorInspector errorInspector) {
        super(regionNormalizer, categoryNormalizer, dateRangeNormalizer, ageConditionNormalizer, keywordNormalizer);
        this.errorInspector = errorInspector;
    }

    public LocalWelfareListResponse parseListResponse(int page, ExternalApiResponse response) {
        errorInspector.assertUsable(PolicySource.LOCAL_WELFARE, page, "XML", response);
        try {
            LocalWelfareListResponse parsed = xmlMapper.readValue(response.body(), LocalWelfareListResponse.class);
            if (parsed.servList() == null) {
                throw new PolicyApiParseException(PolicySource.LOCAL_WELFARE, page, "XML", response.contentType(),
                        response.body(), response.maskedRequestUrl(),
                        "정책 목록 노드를 찾지 못했습니다. 현재 응답 구조가 Swagger 정의와 일치하지 않습니다.");
            }
            return parsed;
        } catch (PolicyApiParseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PolicyApiParseException(PolicySource.LOCAL_WELFARE, page, "XML", response.contentType(),
                    response.body(), response.maskedRequestUrl(), ex);
        }
    }

    public LocalWelfareDetailResponse parseDetailResponse(String serviceId, ExternalApiResponse response) {
        errorInspector.assertUsable(PolicySource.LOCAL_WELFARE, 1, "XML", response);
        try {
            return xmlMapper.readValue(response.body(), LocalWelfareDetailResponse.class);
        } catch (Exception ex) {
            throw new PolicyApiParseException(PolicySource.LOCAL_WELFARE, 1, "XML", response.contentType(),
                    response.body(), response.maskedRequestUrl(), ex);
        }
    }

    public ParsedPolicyPage toPage(LocalWelfareListResponse response) {
        List<PolicyCollectionItem> items = listItems(response).stream().map(item -> toItem(item, null))
                .filter(item -> item.policyName() != null && !item.policyName().isBlank())
                .toList();
        return new ParsedPolicyPage(items, true, totalCount(response), items.isEmpty() ? null : items.get(0).sourcePolicyId(),
                items.isEmpty() ? null : items.get(0).policyName());
    }

    public PolicyCollectionItem toItem(LocalWelfareListItem listItem, LocalWelfareDetailItem detail) {
        return item(PolicySource.LOCAL_WELFARE,
                choose(detail == null ? null : detail.serviceId(), listItem.serviceId()),
                choose(detail == null ? null : detail.serviceName(), listItem.serviceName()),
                choose(detail == null ? null : detail.agencyName(), choose(listItem.agencyName(), listItem.businessDepartmentName())),
                join(choose(listItem.sidoName(), listItem.provinceName()), listItem.sigunguName()),
                join(listItem.serviceCategory(), listItem.lifeNames(), listItem.interestThemeNames()),
                choose(detail == null ? null : detail.supportTarget(), listItem.supportTarget()),
                choose(detail == null ? null : detail.serviceDigest(), listItem.serviceDigest()),
                detail == null ? null : detail.selectionCriteria(),
                choose(detail == null ? null : detail.applicationMethod(), listItem.applicationMethodName()),
                detail == null ? null : detail.requiredDocuments(),
                detail == null ? null : detail.applicationPeriod(),
                choose(detail == null ? null : detail.detailLink(), choose(listItem.detailLink(), listItem.serviceDetailLink())),
                choose(detail == null ? null : detail.inquiryNumber(), listItem.inquiryNumber()));
    }

    public LocalWelfareDetailItem firstDetail(LocalWelfareDetailResponse response) {
        if (response != null && response.serviceId() != null && !response.serviceId().isBlank()) {
            return new LocalWelfareDetailItem(response.serviceId(), response.serviceName(), response.agencyName(),
                    response.supportTarget(), response.selectionCriteria(), response.serviceDigest(),
                    response.applicationMethod(), response.requiredDocuments(), response.applicationPeriod(),
                    response.inquiryNumber(), choose(response.detailLink(), response.serviceDetailLink()));
        }
        if (response == null || response.body() == null || response.body().items() == null
                || response.body().items().item() == null || response.body().items().item().isEmpty()) {
            return null;
        }
        return response.body().items().item().get(0);
    }

    public List<LocalWelfareListItem> listItems(LocalWelfareListResponse response) {
        if (response == null || response.servList() == null) {
            return List.of();
        }
        return response.servList();
    }

    public int totalCount(LocalWelfareListResponse response) {
        return response == null || response.totalCount() == null ? 0 : response.totalCount();
    }

    private String choose(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }
}
