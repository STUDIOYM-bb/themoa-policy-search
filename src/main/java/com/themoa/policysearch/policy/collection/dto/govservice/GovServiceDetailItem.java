package com.themoa.policysearch.policy.collection.dto.govservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GovServiceDetailItem(
        @JsonProperty("서비스ID") String serviceId,
        @JsonProperty("지원유형") String supportType,
        @JsonProperty("서비스명") String serviceName,
        @JsonProperty("서비스목적") String servicePurpose,
        @JsonProperty("신청기한") String applicationPeriod,
        @JsonProperty("지원대상") String supportTarget,
        @JsonProperty("선정기준") String selectionCriteria,
        @JsonProperty("지원내용") String supportContent,
        @JsonProperty("신청방법") String applicationMethod,
        @JsonProperty("구비서류") String requiredDocuments,
        @JsonProperty("접수기관명") String receptionAgencyName,
        @JsonProperty("문의처") String contact,
        @JsonProperty("온라인신청사이트URL") String onlineApplicationUrl,
        @JsonProperty("수정일시") String updatedAt,
        @JsonProperty("소관기관명") String agencyName,
        @JsonProperty("행정규칙") String administrativeRule,
        @JsonProperty("자치법규") String localRule,
        @JsonProperty("법령") String law,
        @JsonProperty("공무원확인구비서류") String officialCheckDocuments,
        @JsonProperty("본인확인필요구비서류") String selfCheckDocuments
) {
}
