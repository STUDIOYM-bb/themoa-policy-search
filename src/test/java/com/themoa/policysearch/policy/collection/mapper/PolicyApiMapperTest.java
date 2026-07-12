package com.themoa.policysearch.policy.collection.mapper;

import com.themoa.policysearch.policy.collection.client.ExternalApiResponse;
import com.themoa.policysearch.policy.collection.client.PolicyApiParseException;
import com.themoa.policysearch.policy.collection.client.PolicyApiResponseException;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.normalizer.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyApiMapperTest {
    private final RegionNormalizer regionNormalizer = new RegionNormalizer();
    private final CategoryNormalizer categoryNormalizer = new CategoryNormalizer();
    private final DateRangeNormalizer dateRangeNormalizer = new DateRangeNormalizer();
    private final AgeConditionNormalizer ageConditionNormalizer = new AgeConditionNormalizer();
    private final KeywordNormalizer keywordNormalizer = new KeywordNormalizer();
    private final ExternalApiErrorInspector errorInspector = new ExternalApiErrorInspector();

    @Test
    void parsesGovServiceListDetailAndSupportConditionsThenMerges() throws Exception {
        GovServicePolicyMapper mapper = govMapper();

        var list = mapper.parseListResponse(1, json(fixture("gov-service-list-success.json")));
        var detail = mapper.firstDetail(mapper.parseDetailResponse("GOV-1", json(fixture("gov-service-detail-success.json"))));
        var conditions = mapper.firstSupportCondition(mapper.parseSupportConditionsResponse("GOV-1",
                json(fixture("gov-service-support-conditions-success.json"))));

        PolicyCollectionItem item = mapper.toItem(list.data().get(0), detail, conditions);

        assertThat(item.sourcePolicyId()).isEqualTo("GOV-1");
        assertThat(item.policyName()).isEqualTo("공공서비스 혜택 상세");
        assertThat(item.minimumAge()).isEqualTo(19);
        assertThat(item.maximumAge()).isEqualTo(34);
        assertThat(item.applicationMethod()).isEqualTo("정부24 온라인 신청");
        assertThat(item.requiredDocuments()).isEqualTo("신분증");
    }

    @Test
    void detectsGovServiceJsonError() throws Exception {
        assertThatThrownBy(() -> govMapper().parseListResponse(1, json(fixture("gov-service-error.json"))))
                .isInstanceOf(PolicyApiResponseException.class)
                .hasMessageContaining("INVALID_KEY");
    }

    @Test
    void parsesLocalWelfareListDetailAndMerges() throws Exception {
        LocalWelfarePolicyMapper mapper = localMapper();

        var list = mapper.parseListResponse(1, xml(fixture("local-welfare-list-success.xml")));
        var detail = mapper.firstDetail(mapper.parseDetailResponse("L-1", xml(fixture("local-welfare-detail-success.xml"))));
        PolicyCollectionItem item = mapper.toItem(mapper.listItems(list).get(0), detail);

        assertThat(item.sourcePolicyId()).isEqualTo("L-1");
        assertThat(item.policyName()).isEqualTo("지역 복지 서비스");
        assertThat(item.selectionCriteria()).contains("소득");
        assertThat(item.applicationMethod()).isEqualTo("방문 신청");
    }

    @Test
    void parsesCentralWelfareListDetailAndMerges() throws Exception {
        CentralWelfarePolicyMapper mapper = centralMapper();

        var list = mapper.parseListResponse(1, xml(fixture("central-welfare-list-success.xml")));
        var detail = mapper.firstDetail(mapper.parseDetailResponse("C-1", xml(fixture("central-welfare-detail-success.xml"))));
        PolicyCollectionItem item = mapper.toItem(mapper.listItems(list).get(0), detail);

        assertThat(item.sourcePolicyId()).isEqualTo("C-1");
        assertThat(item.policyName()).isEqualTo("중앙 복지 서비스");
        assertThat(item.targetGroups()).contains("전 국민");
        assertThat(item.applicationMethod()).isEqualTo("온라인 신청");
    }

    @Test
    void detectsInternalXmlError() throws Exception {
        assertThatThrownBy(() -> localMapper().parseListResponse(1, xml(fixture("data-go-auth-error.xml"))))
                .isInstanceOf(PolicyApiResponseException.class)
                .hasMessageContaining("30")
                .hasMessageContaining("SERVICE_KEY");
    }

    @Test
    void detectsHtmlBodyEvenWhenContentTypeIsWrong() throws Exception {
        assertThatThrownBy(() -> centralMapper().parseListResponse(1,
                new ExternalApiResponse(200, "application/xml", fixture("html-error-response.html"), "https://example.test?serviceKey=****")))
                .isInstanceOf(PolicyApiParseException.class)
                .hasMessageContaining("HTML");
    }

    @Test
    void missingListNodeIsSchemaErrorButEmptyListIsValid() {
        assertThatThrownBy(() -> govMapper().parseListResponse(1, json("{\"totalCount\":1,\"items\":[]}")))
                .isInstanceOf(PolicyApiParseException.class)
                .hasMessageContaining("정책 목록 노드");

        var parsed = govMapper().parseListResponse(1, json("{\"totalCount\":0,\"data\":[]}"));

        assertThat(parsed.data()).isEmpty();
    }

    private GovServicePolicyMapper govMapper() {
        return new GovServicePolicyMapper(regionNormalizer, categoryNormalizer, dateRangeNormalizer,
                ageConditionNormalizer, keywordNormalizer, errorInspector);
    }

    private LocalWelfarePolicyMapper localMapper() {
        return new LocalWelfarePolicyMapper(regionNormalizer, categoryNormalizer, dateRangeNormalizer,
                ageConditionNormalizer, keywordNormalizer, errorInspector);
    }

    private CentralWelfarePolicyMapper centralMapper() {
        return new CentralWelfarePolicyMapper(regionNormalizer, categoryNormalizer, dateRangeNormalizer,
                ageConditionNormalizer, keywordNormalizer, errorInspector);
    }

    private ExternalApiResponse json(String body) {
        return new ExternalApiResponse(200, "application/json;charset=UTF-8", body, "https://example.test?serviceKey=****");
    }

    private ExternalApiResponse xml(String body) {
        return new ExternalApiResponse(200, "text/xml", body, "https://example.test?serviceKey=****");
    }

    private String fixture(String name) throws Exception {
        return new ClassPathResource("fixtures/" + name).getContentAsString(StandardCharsets.UTF_8);
    }
}
