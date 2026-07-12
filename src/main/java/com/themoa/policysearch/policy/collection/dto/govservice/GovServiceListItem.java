package com.themoa.policysearch.policy.collection.dto.govservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GovServiceListItem(
        @JsonProperty("서비스ID") String serviceId,
        @JsonProperty("지원유형") String supportType,
        @JsonProperty("서비스명") String serviceName,
        @JsonProperty("서비스목적요약") String servicePurposeSummary,
        @JsonProperty("지원대상") String supportTarget,
        @JsonProperty("선정기준") String selectionCriteria,
        @JsonProperty("지원내용") String supportContent,
        @JsonProperty("신청방법") String applicationMethod,
        @JsonProperty("신청기한") String applicationPeriod,
        @JsonProperty("상세조회URL") String detailUrl,
        @JsonProperty("소관기관코드") String agencyCode,
        @JsonProperty("소관기관명") String agencyName,
        @JsonProperty("부서명") String departmentName,
        @JsonProperty("소관기관유형") String agencyType,
        @JsonProperty("사용자구분") String userType,
        @JsonProperty("서비스분야") String serviceCategory,
        @JsonProperty("접수기관") String receptionAgency,
        @JsonProperty("전화문의") String contact,
        @JsonProperty("등록일시") String createdAt,
        @JsonProperty("수정일시") String updatedAt
) {
}
