package com.themoa.policysearch.policy.collection.dto.localwelfare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LocalWelfareListItem(
        @JacksonXmlProperty(localName = "servId") String serviceId,
        @JacksonXmlProperty(localName = "servNm") String serviceName,
        @JacksonXmlProperty(localName = "jurMnofNm") String agencyName,
        @JacksonXmlProperty(localName = "bizChrDeptNm") String businessDepartmentName,
        @JacksonXmlProperty(localName = "sidoNm") String sidoName,
        @JacksonXmlProperty(localName = "ctpvNm") String provinceName,
        @JacksonXmlProperty(localName = "sggNm") String sigunguName,
        @JacksonXmlProperty(localName = "servSeNm") String serviceCategory,
        @JacksonXmlProperty(localName = "lifeNmArray") String lifeNames,
        @JacksonXmlProperty(localName = "intrsThemaNmArray") String interestThemeNames,
        @JacksonXmlProperty(localName = "servDgst") String serviceDigest,
        @JacksonXmlProperty(localName = "sprtTrgtCn") String supportTarget,
        @JacksonXmlProperty(localName = "servDtlLink") String serviceDetailLink,
        @JacksonXmlProperty(localName = "servSeDetailLink") String detailLink,
        @JacksonXmlProperty(localName = "aplyMtdNm") String applicationMethodName,
        @JacksonXmlProperty(localName = "inqNum") String inquiryNumber,
        @JacksonXmlProperty(localName = "lastModYmd") String lastModifiedYmd
) {
}
