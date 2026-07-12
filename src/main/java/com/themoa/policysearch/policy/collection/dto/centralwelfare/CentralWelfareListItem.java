package com.themoa.policysearch.policy.collection.dto.centralwelfare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CentralWelfareListItem(
        @JacksonXmlProperty(localName = "servId") String serviceId,
        @JacksonXmlProperty(localName = "servNm") String serviceName,
        @JacksonXmlProperty(localName = "jurMnofNm") String agencyName,
        @JacksonXmlProperty(localName = "lifeArray") String lifeArray,
        @JacksonXmlProperty(localName = "lifeNmArray") String lifeNames,
        @JacksonXmlProperty(localName = "intrsThemaArray") String interestThemeArray,
        @JacksonXmlProperty(localName = "intrsThemaNmArray") String interestThemeNames,
        @JacksonXmlProperty(localName = "servDgst") String serviceDigest,
        @JacksonXmlProperty(localName = "sprtTrgtCn") String supportTarget,
        @JacksonXmlProperty(localName = "servDtlLink") String serviceDetailLink,
        @JacksonXmlProperty(localName = "servSeDetailLink") String detailLink,
        @JacksonXmlProperty(localName = "inqNum") String inquiryNumber,
        @JacksonXmlProperty(localName = "lastModYmd") String lastModifiedYmd
) {
}
