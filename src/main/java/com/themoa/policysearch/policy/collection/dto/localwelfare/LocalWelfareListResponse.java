package com.themoa.policysearch.policy.collection.dto.localwelfare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "wantedList")
public record LocalWelfareListResponse(
        @JacksonXmlProperty(localName = "resultCode") String resultCode,
        @JacksonXmlProperty(localName = "resultMessage") String resultMessage,
        @JacksonXmlProperty(localName = "pageNo") Integer pageNo,
        @JacksonXmlProperty(localName = "numOfRows") Integer numOfRows,
        @JacksonXmlProperty(localName = "totalCount") Integer totalCount,
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "servList")
        List<LocalWelfareListItem> servList
) {
}
