package com.themoa.policysearch.policy.collection.normalizer;

import com.themoa.policysearch.policy.domain.PolicyCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyNormalizersTest {
    @Test
    void regionNormalizerExpandsSuwon() {
        RegionNormalizer normalizer = new RegionNormalizer();
        assertThat(normalizer.normalize("수원 청년 정책")).containsExactly("경기도 수원시");
        assertThat(normalizer.matchesStrict("경기도 수원시", java.util.List.of("경기도"))).isTrue();
        assertThat(normalizer.matchesStrict("경기도 수원시", java.util.List.of("서울특별시"))).isFalse();
    }

    @Test
    void regionNormalizerDoesNotTreatAgencyAsRegion() {
        RegionNormalizer normalizer = new RegionNormalizer();
        assertThat(normalizer.normalize("농림축산식품부 중앙행정기관")).isEmpty();
        assertThat(normalizer.normalize("보건복지부")).isEmpty();
        assertThat(normalizer.normalize("경기도 수원시")).containsExactly("경기도 수원시");
        assertThat(normalizer.normalize("전국")).containsExactly("전국");
    }

    @Test
    void categoryNormalizerMapsKeywords() {
        CategoryNormalizer normalizer = new CategoryNormalizer();
        assertThat(normalizer.normalize("청년 월세 지원")).isEqualTo(PolicyCategory.주거);
        assertThat(normalizer.normalize("생활비 지원금")).isEqualTo(PolicyCategory.생활지원);
    }

    @Test
    void dateRangeNormalizerParsesPeriod() {
        DateRangeNormalizer normalizer = new DateRangeNormalizer();
        DateRange range = normalizer.normalize("2026.07.01 ~ 2026.07.31");
        assertThat(range.startDate()).hasToString("2026-07-01");
        assertThat(range.endDate()).hasToString("2026-07-31");
    }

    @Test
    void ageConditionNormalizerParsesYouthFallback() {
        AgeConditionNormalizer normalizer = new AgeConditionNormalizer();
        assertThat(normalizer.normalize("청년 대상").minimumAge()).isEqualTo(19);
        assertThat(normalizer.normalize("만 20세 이상 39세 이하").maximumAge()).isEqualTo(39);
    }
}
