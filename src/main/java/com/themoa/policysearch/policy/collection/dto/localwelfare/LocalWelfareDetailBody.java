package com.themoa.policysearch.policy.collection.dto.localwelfare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LocalWelfareDetailBody(
        @JacksonXmlProperty(localName = "items") LocalWelfareDetailItems items
) {
}
