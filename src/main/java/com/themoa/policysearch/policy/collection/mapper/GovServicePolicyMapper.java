package com.themoa.policysearch.policy.collection.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themoa.policysearch.policy.collection.client.ExternalApiResponse;
import com.themoa.policysearch.policy.collection.client.PolicyApiParseException;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.dto.govservice.*;
import com.themoa.policysearch.policy.collection.normalizer.*;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GovServicePolicyMapper extends PolicyMappingSupport {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExternalApiErrorInspector errorInspector;

    public GovServicePolicyMapper(RegionNormalizer regionNormalizer, CategoryNormalizer categoryNormalizer,
                                  DateRangeNormalizer dateRangeNormalizer, AgeConditionNormalizer ageConditionNormalizer,
                                  KeywordNormalizer keywordNormalizer, ExternalApiErrorInspector errorInspector) {
        super(regionNormalizer, categoryNormalizer, dateRangeNormalizer, ageConditionNormalizer, keywordNormalizer);
        this.errorInspector = errorInspector;
    }

    public GovServiceListResponse parseListResponse(int page, ExternalApiResponse response) {
        errorInspector.assertUsable(PolicySource.GOV_SERVICE, page, "JSON", response);
        try {
            GovServiceListResponse parsed = objectMapper.readValue(response.body(), GovServiceListResponse.class);
            if (parsed.data() == null) {
                throw new PolicyApiParseException(PolicySource.GOV_SERVICE, page, "JSON", response.contentType(),
                        response.body(), response.maskedRequestUrl(),
                        "정책 목록 노드를 찾지 못했습니다. 현재 응답 구조가 Swagger 정의와 일치하지 않습니다.");
            }
            return parsed;
        } catch (PolicyApiParseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PolicyApiParseException(PolicySource.GOV_SERVICE, page, "JSON", response.contentType(),
                    response.body(), response.maskedRequestUrl(), ex);
        }
    }

    public GovServiceDetailResponse parseDetailResponse(String serviceId, ExternalApiResponse response) {
        errorInspector.assertUsable(PolicySource.GOV_SERVICE, 1, "JSON", response);
        try {
            return objectMapper.readValue(response.body(), GovServiceDetailResponse.class);
        } catch (Exception ex) {
            throw new PolicyApiParseException(PolicySource.GOV_SERVICE, 1, "JSON", response.contentType(),
                    response.body(), response.maskedRequestUrl(), ex);
        }
    }

    public GovServiceSupportConditionsResponse parseSupportConditionsResponse(String serviceId, ExternalApiResponse response) {
        errorInspector.assertUsable(PolicySource.GOV_SERVICE, 1, "JSON", response);
        try {
            return objectMapper.readValue(response.body(), GovServiceSupportConditionsResponse.class);
        } catch (Exception ex) {
            throw new PolicyApiParseException(PolicySource.GOV_SERVICE, 1, "JSON", response.contentType(),
                    response.body(), response.maskedRequestUrl(), ex);
        }
    }

    public ParsedPolicyPage toPage(GovServiceListResponse response) {
        List<PolicyCollectionItem> items = new ArrayList<>();
        for (GovServiceListItem item : safeList(response.data())) {
            PolicyCollectionItem mapped = toItem(item, null, null);
            if (mapped.policyName() != null && !mapped.policyName().isBlank()) {
                items.add(mapped);
            }
        }
        return new ParsedPolicyPage(items, true, intValue(response.totalCount()),
                items.isEmpty() ? null : items.get(0).sourcePolicyId(),
                items.isEmpty() ? null : items.get(0).policyName());
    }

    public PolicyCollectionItem toItem(GovServiceListItem listItem, GovServiceDetailItem detail,
                                       GovServiceSupportConditionItem supportCondition) {
        String id = choose(detail == null ? null : detail.serviceId(), listItem.serviceId());
        String name = choose(detail == null ? null : detail.serviceName(), listItem.serviceName());
        String organization = choose(detail == null ? null : detail.agencyName(), listItem.agencyName());
        String support = choose(detail == null ? null : detail.supportContent(),
                choose(listItem.supportContent(), listItem.servicePurposeSummary()));
        String criteria = choose(detail == null ? null : detail.selectionCriteria(), listItem.selectionCriteria());
        PolicyCollectionItem base = item(PolicySource.GOV_SERVICE, id, name, organization,
                "전국",
                join(listItem.serviceCategory(), listItem.supportType(), listItem.userType()),
                choose(detail == null ? null : detail.supportTarget(), listItem.supportTarget()),
                support,
                join(criteria, supportConditionText(supportCondition)),
                choose(detail == null ? null : detail.applicationMethod(), listItem.applicationMethod()),
                choose(detail == null ? null : detail.requiredDocuments(), null),
                choose(detail == null ? null : detail.applicationPeriod(), listItem.applicationPeriod()),
                choose(detail == null ? null : detail.onlineApplicationUrl(), listItem.detailUrl()),
                choose(detail == null ? null : detail.contact(), listItem.contact()));
        if (supportCondition == null) {
            return base;
        }
        Integer minimumAge = supportCondition.minimumAge() == null ? base.minimumAge() : supportCondition.minimumAge();
        Integer maximumAge = supportCondition.maximumAge() == null ? base.maximumAge() : supportCondition.maximumAge();
        return new PolicyCollectionItem(base.source(), base.sourcePolicyId(), base.policyName(), base.organization(),
                base.managingOrganization(), base.regionNames(), base.regionCodes(), base.category(), base.targetGroups(),
                minimumAge, maximumAge, employmentText(supportCondition), studentCondition(supportCondition),
                incomeText(supportCondition), housingText(supportCondition), householdText(supportCondition),
                base.selectionCriteria(), base.supportContent(), base.applicationMethod(), base.requiredDocuments(),
                base.applicationStartDate(), base.applicationEndDate(), base.applicationPeriodText(), base.alwaysOpen(),
                base.officialUrl(), base.contact(), base.keywords(), base.sourceUpdatedAt(), base.collectedAt(), base.rawDataId());
    }

    public GovServiceDetailItem firstDetail(GovServiceDetailResponse response) {
        return safeList(response == null ? null : response.data()).stream().findFirst().orElse(null);
    }

    public GovServiceSupportConditionItem firstSupportCondition(GovServiceSupportConditionsResponse response) {
        return safeList(response == null ? null : response.data()).stream().findFirst().orElse(null);
    }

    private String supportConditionText(GovServiceSupportConditionItem item) {
        if (item == null) {
            return null;
        }
        return join(incomeText(item), employmentText(item), housingText(item), householdText(item));
    }

    private String incomeText(GovServiceSupportConditionItem item) {
        return join(enabled(item.income0To50(), "중위소득 0~50%"),
                enabled(item.income51To75(), "중위소득 51~75%"),
                enabled(item.income76To100(), "중위소득 76~100%"),
                enabled(item.income101To200(), "중위소득 101~200%"),
                enabled(item.incomeOver200(), "중위소득 200% 초과"));
    }

    private String employmentText(GovServiceSupportConditionItem item) {
        return join(enabled(item.worker(), "근로자/직장인"), enabled(item.jobSeeker(), "구직자/실업자"));
    }

    private Boolean studentCondition(GovServiceSupportConditionItem item) {
        return hasText(item.elementaryStudent()) || hasText(item.middleStudent()) || hasText(item.highStudent())
                || hasText(item.collegeStudent()) ? true : null;
    }

    private String housingText(GovServiceSupportConditionItem item) {
        return enabled(item.homelessHousehold(), "무주택세대");
    }

    private String householdText(GovServiceSupportConditionItem item) {
        return join(enabled(item.singleParent(), "한부모가정/조손가정"),
                enabled(item.singleHousehold(), "1인가구"),
                enabled(item.multiChildHousehold(), "다자녀가구"));
    }

    private String enabled(String value, String label) {
        return hasText(value) && !"N".equalsIgnoreCase(value) && !"0".equals(value) ? label : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private int intValue(Integer value) {
        return value == null ? 0 : value;
    }

    private String choose(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }
}
