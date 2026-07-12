package com.themoa.policysearch.policy.rag.document;

import com.themoa.policysearch.policy.domain.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyDocumentBuilderTest {
    @Test
    void buildsDocumentWithoutNullLiterals() {
        Policy policy = new Policy("청년 월세 지원", "P-1", PolicySource.LOCAL_WELFARE, "수원시", PolicyCategory.주거);
        policy.replaceCondition(new PolicyCondition(19, 34, null, null, "중위소득 150% 이하", "청년 대상", true));
        policy.updateBasic("청년 월세 지원", "수원시", PolicyCategory.주거, "월세 일부 지원", null, null, null, false, ApplicationStatus.NEEDS_CONFIRMATION);
        PolicyDocument document = new PolicyDocumentBuilder().build(policy);
        assertThat(document.content()).contains("[정책명]", "청년 월세 지원");
        assertThat(document.content()).doesNotContain("null");
        assertThat(document.contentHash()).hasSize(64);
    }

    @Test
    void usesDeterministicUuidDocumentIdAndRemovesNullMetadata() throws Exception {
        Policy policy = new Policy("청년 월세 지원", "P-1", PolicySource.LOCAL_WELFARE, "수원시", PolicyCategory.주거);
        setId(policy, 7);
        policy.updateBasic("청년 월세 지원", "수원시", PolicyCategory.주거, "월세 일부 지원", null, null, null, false, ApplicationStatus.NEEDS_CONFIRMATION);

        PolicyDocument document = new PolicyDocumentBuilder().build(policy);

        assertThat(document.id()).isEqualTo(UUID.nameUUIDFromBytes("policy:7".getBytes(StandardCharsets.UTF_8)).toString());
        assertThat(document.metadata()).containsEntry("policyId", 7);
        assertThat(document.metadata()).doesNotContainValue(null);
    }

    private void setId(Policy policy, Integer id) throws Exception {
        Field field = Policy.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(policy, id);
    }
}
