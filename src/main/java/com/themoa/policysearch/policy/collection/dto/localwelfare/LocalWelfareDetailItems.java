package com.themoa.policysearch.policy.collection.dto.localwelfare;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LocalWelfareDetailItems(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        List<LocalWelfareDetailItem> item
) {
}
