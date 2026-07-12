package com.themoa.policysearch.policy.collection.service;

import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.domain.*;
import com.themoa.policysearch.policy.repository.PolicyRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicyDeduplicationServiceTest {
    @Test
    void sameSourceIdIsDuplicate() {
        PolicyRepository repository = mock(PolicyRepository.class);
        Policy policy = new Policy("청년 월세", "A1", PolicySource.YOUTH_CENTER, "수원시", PolicyCategory.주거);
        when(repository.findBySourceTypeAndSourcePolicyId(PolicySource.YOUTH_CENTER, "A1")).thenReturn(Optional.of(policy));
        PolicyDeduplicationService service = new PolicyDeduplicationService(repository);
        assertThat(service.findSameSourceDuplicate(item("A1"))).contains(policy);
    }

    @Test
    void similarNameDifferentAgencyIsNotAutomaticallyMerged() {
        PolicyRepository repository = mock(PolicyRepository.class);
        Policy left = new Policy("청년 월세 지원", "A1", PolicySource.YOUTH_CENTER, "수원시", PolicyCategory.주거);
        PolicyDeduplicationService service = new PolicyDeduplicationService(repository);
        assertThat(service.isLikelySamePolicy(left, item("B1"))).isFalse();
    }

    private PolicyCollectionItem item(String id) {
        return new PolicyCollectionItem(PolicySource.YOUTH_CENTER, id, "청년 월세 지원", "서울시", "서울시",
                List.of("서울특별시"), List.of(), PolicyCategory.주거, List.of("청년"), 19, 34,
                null, null, null, null, null, "기준", "내용", "온라인", null,
                null, null, "상시", true, null, null, List.of("청년"), null, LocalDateTime.now(), null);
    }
}
