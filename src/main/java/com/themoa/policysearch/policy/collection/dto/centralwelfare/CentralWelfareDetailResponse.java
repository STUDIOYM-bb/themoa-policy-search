package com.themoa.policysearch.policy.collection.dto.centralwelfare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "wantedDtl")
public record CentralWelfareDetailResponse(
        @JacksonXmlProperty(localName = "servId") String serviceId,
        @JacksonXmlProperty(localName = "servNm") String serviceName,
        @JacksonXmlProperty(localName = "jurMnofNm") String agencyName,
        @JacksonXmlProperty(localName = "sprtTrgtCn") String supportTarget,
        @JacksonXmlProperty(localName = "slctCritCn") String selectionCriteria,
        @JacksonXmlProperty(localName = "servDgst") String serviceDigest,
        @JacksonXmlProperty(localName = "aplyMtdCn") String applicationMethod,
        @JacksonXmlProperty(localName = "sbmsnDcmntCn") String requiredDocuments,
        @JacksonXmlProperty(localName = "aplyPrdCn") String applicationPeriod,
        @JacksonXmlProperty(localName = "inqNum") String inquiryNumber,
        @JacksonXmlProperty(localName = "servDtlLink") String serviceDetailLink,
        @JacksonXmlProperty(localName = "servSeDetailLink") String detailLink,
        @JacksonXmlProperty(localName = "header") CentralWelfareResponseHeader header,
        @JacksonXmlProperty(localName = "body") CentralWelfareDetailBody body
) {
}
