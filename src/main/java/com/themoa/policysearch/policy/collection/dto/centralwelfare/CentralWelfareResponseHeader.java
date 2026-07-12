package com.themoa.policysearch.policy.collection.dto.centralwelfare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CentralWelfareResponseHeader(
        @JacksonXmlProperty(localName = "resultCode") String resultCode,
        @JacksonXmlProperty(localName = "resultMsg") String resultMsg
) {
}
