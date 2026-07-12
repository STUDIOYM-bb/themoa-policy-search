package com.themoa.policysearch.policy.collection.dto.centralwelfare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CentralWelfareListBody(
        @JacksonXmlProperty(localName = "pageNo") Integer pageNo,
        @JacksonXmlProperty(localName = "numOfRows") Integer numOfRows,
        @JacksonXmlProperty(localName = "totalCount") Integer totalCount,
        @JacksonXmlProperty(localName = "items") CentralWelfareListItems items
) {
}
