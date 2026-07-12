package com.themoa.policysearch.policy.collection.dto.govservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GovServiceSupportConditionItem(
        @JsonProperty("서비스ID") String serviceId,
        @JsonProperty("서비스명") String serviceName,
        @JsonProperty("JA0110") Integer minimumAge,
        @JsonProperty("JA0111") Integer maximumAge,
        @JsonProperty("JA0201") String income0To50,
        @JsonProperty("JA0202") String income51To75,
        @JsonProperty("JA0203") String income76To100,
        @JsonProperty("JA0204") String income101To200,
        @JsonProperty("JA0205") String incomeOver200,
        @JsonProperty("JA0317") String elementaryStudent,
        @JsonProperty("JA0318") String middleStudent,
        @JsonProperty("JA0319") String highStudent,
        @JsonProperty("JA0320") String collegeStudent,
        @JsonProperty("JA0326") String worker,
        @JsonProperty("JA0327") String jobSeeker,
        @JsonProperty("JA0403") String singleParent,
        @JsonProperty("JA0404") String singleHousehold,
        @JsonProperty("JA0411") String multiChildHousehold,
        @JsonProperty("JA0412") String homelessHousehold
) {
}
