package com.themoa.policysearch.policy.collection.dto.localwelfare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LocalWelfareDetailItem(
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
        @JacksonXmlProperty(localName = "servSeDetailLink") String detailLink
) {
}
