package com.themoa.policysearch.policy.collection.dto.centralwelfare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CentralWelfareDetailItems(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        List<CentralWelfareDetailItem> item
) {
}
