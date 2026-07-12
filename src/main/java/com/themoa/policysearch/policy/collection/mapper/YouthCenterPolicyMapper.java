package com.themoa.policysearch.policy.collection.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.themoa.policysearch.policy.collection.client.ExternalApiResponse;
import com.themoa.policysearch.policy.collection.client.PolicyApiParseException;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.normalizer.*;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class YouthCenterPolicyMapper extends PolicyMappingSupport {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExternalApiErrorInspector errorInspector;

    public YouthCenterPolicyMapper(RegionNormalizer regionNormalizer, CategoryNormalizer categoryNormalizer,
                                   DateRangeNormalizer dateRangeNormalizer, AgeConditionNormalizer ageConditionNormalizer,
                                   KeywordNormalizer keywordNormalizer, ExternalApiErrorInspector errorInspector) {
        super(regionNormalizer, categoryNormalizer, dateRangeNormalizer, ageConditionNormalizer, keywordNormalizer);
        this.errorInspector = errorInspector;
    }

    public ParsedPolicyPage parse(int page, ExternalApiResponse response) {
        errorInspector.assertUsable(PolicySource.YOUTH_CENTER, page, "JSON", response);
        try {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode list = root.get("youthPolicyList");
            if (list == null) {
                JsonNode result = root.get("result");
                list = result == null ? null : result.get("youthPolicyList");
            }
            if (list == null) {
                throw new PolicyApiParseException(PolicySource.YOUTH_CENTER, page, "JSON", response.contentType(),
                        response.body(), response.maskedRequestUrl(), "정책 목록 노드를 찾지 못했습니다. 응답 스키마가 예상과 다릅니다.");
            }
            List<PolicyCollectionItem> items = new ArrayList<>();
            if (list.isArray()) {
                list.forEach(node -> addMapped(items, node));
            } else if (list.isObject()) {
                addMapped(items, list);
            }
            int totalCount = intValue(root, "totalCnt", "totalCount");
            String firstName = items.isEmpty() ? null : items.get(0).policyName();
            String firstId = items.isEmpty() ? null : items.get(0).sourcePolicyId();
            return new ParsedPolicyPage(items, true, totalCount, firstId, firstName);
        } catch (PolicyApiParseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PolicyApiParseException(PolicySource.YOUTH_CENTER, page, "JSON", response.contentType(),
                    response.body(), response.maskedRequestUrl(), ex);
        }
    }

    private void addMapped(List<PolicyCollectionItem> items, JsonNode node) {
        PolicyCollectionItem item = item(PolicySource.YOUTH_CENTER,
                firstText(node, "plcyNo", "bizId", "policyId"),
                firstText(node, "plcyNm", "policyName", "title"),
                firstText(node, "sprvsnInstCdNm", "operInstCdNm", "agencyName"),
                firstText(node, "zipCd", "region", "areaNm"),
                firstText(node, "plcyKywdNm", "lclsfNm", "bizTycdSel"),
                firstText(node, "sprtTrgtCn", "target", "lifeArray"),
                firstText(node, "plcySprtCn", "supportContent", "summary"),
                firstText(node, "aplyQlfcCn", "selectionCriteria", "condition"),
                firstText(node, "aplyMthdCn", "applicationMethod"),
                firstText(node, "sbmsnDcmntCn", "requiredDocuments"),
                firstText(node, "aplyYmd", "applicationPeriod"),
                firstText(node, "refUrlAddr1", "refUrlAddr2", "url"),
                firstText(node, "inqryTelno", "contact"));
        if (item.policyName() != null && !item.policyName().isBlank()) {
            items.add(item);
        }
    }

    private int intValue(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && value.canConvertToInt()) {
                return value.asInt();
            }
            if (value != null && value.isTextual()) {
                try {
                    return Integer.parseInt(value.asText());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }
}
