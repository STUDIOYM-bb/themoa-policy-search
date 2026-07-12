package com.themoa.policysearch.policy.search.retrieval;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QdrantPolicyRetrievalServiceTest {
    @Test
    void returnsRagModeOnlyWhenVectorStoreSearchSucceeds() {
        VectorStore vectorStore = mock(VectorStore.class);
        ObjectProvider<VectorStore> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(vectorStore);
        Document document = Document.builder()
                .id("doc-1")
                .text("청년 월세 지원")
                .metadata(Map.of("policyId", 10, "active", true))
                .score(0.823)
                .build();
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(document));

        PolicyRetrievalResult result = new QdrantPolicyRetrievalService(provider, true)
                .retrieve("수원 청년 월세 지원", 10);

        assertThat(result.searchMode()).isEqualTo(PolicySearchMode.RAG);
        assertThat(result.ragAttempted()).isTrue();
        assertThat(result.ragSucceeded()).isTrue();
        assertThat(result.candidates()).hasSize(1);
        assertThat(result.candidates().get(0).policyId()).isEqualTo(10);
        assertThat(result.candidates().get(0).semanticScore()).isEqualTo(0.823);
        verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void failsClearlyWhenVectorStoreIsUnavailable() {
        ObjectProvider<VectorStore> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> new QdrantPolicyRetrievalService(provider, true).retrieve("수원 청년", 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VectorStore");
    }
}
