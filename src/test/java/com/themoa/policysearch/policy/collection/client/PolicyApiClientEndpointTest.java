package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyApiClientEndpointTest {
    @Test
    void buildsGovServiceUrls() {
        PolicyCollectionProperties props = props();
        GovServiceApiClient client = new GovServiceApiClient(new ExternalPolicyApiClient(props), props);

        assertThat(client.listRequestUrl(2, 5))
                .startsWith("https://api.odcloud.kr/api/gov24/v3/serviceList")
                .contains("serviceKey=****")
                .contains("page=2")
                .contains("perPage=5")
                .doesNotContain("LocalGovernmentWelfareInformations");
        assertThat(client.detailRequestUrl("GOV-1"))
                .startsWith("https://api.odcloud.kr/api/gov24/v3/serviceDetail")
                .contains("cond%5B%EC%84%9C%EB%B9%84%EC%8A%A4ID%3A%3AEQ%5D=GOV-1");
        assertThat(client.supportConditionsRequestUrl("GOV-1"))
                .startsWith("https://api.odcloud.kr/api/gov24/v3/supportConditions");
    }

    @Test
    void buildsWelfareUrls() {
        PolicyCollectionProperties props = props();
        LocalWelfareApiClient local = new LocalWelfareApiClient(new ExternalPolicyApiClient(props), props);
        CentralWelfareApiClient central = new CentralWelfareApiClient(new ExternalPolicyApiClient(props), props);

        assertThat(local.listRequestUrl(1, 10))
                .startsWith("https://apis.data.go.kr/B554287/LocalGovernmentWelfareInformations/LcgvWelfarelist")
                .contains("serviceKey=****")
                .contains("pageNo=1")
                .contains("numOfRows=10");
        assertThat(local.detailRequestUrl("L-1"))
                .startsWith("https://apis.data.go.kr/B554287/LocalGovernmentWelfareInformations/LcgvWelfaredetailed")
                .contains("servId=L-1");
        assertThat(central.listRequestUrl(1, 10))
                .startsWith("https://apis.data.go.kr/B554287/NationalWelfareInformationsV001/NationalWelfarelistV001");
        assertThat(central.detailRequestUrl("C-1"))
                .startsWith("https://apis.data.go.kr/B554287/NationalWelfareInformationsV001/NationalWelfaredetailedV001")
                .contains("servId=C-1");
    }

    @Test
    void emptyApiKeyFailsBeforeCall() {
        PolicyCollectionProperties props = props();
        props.getGovService().setApiKey("");
        GovServiceApiClient client = new GovServiceApiClient(new ExternalPolicyApiClient(props), props);

        assertThatThrownBy(() -> client.fetchServiceList(1, 1))
                .isInstanceOf(PolicyApiException.class)
                .hasMessageContaining("API key");
    }

    @Test
    void http401And403PreserveBodyAndMaskedUrl() throws Exception {
        PolicyCollectionProperties props = props();
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setResponseCode(401).setHeader("Content-Type", "application/json")
                    .setBody("{\"error\":\"UNAUTHORIZED\",\"message\":\"bad key\"}"));
            props.getGovService().setBaseUrl(server.url("/api").toString());
            GovServiceApiClient client = new GovServiceApiClient(new ExternalPolicyApiClient(props), props);

            assertThatThrownBy(() -> client.fetchServiceList(1, 1))
                    .isInstanceOf(PolicyApiResponseException.class)
                    .hasMessageContaining("HTTP 401")
                    .hasMessageContaining("serviceKey=****")
                    .satisfies(ex -> assertThat(ex.getMessage()).doesNotContain("test-key"));
        }
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setResponseCode(403).setHeader("Content-Type", "application/json")
                    .setBody("{\"error\":\"FORBIDDEN\",\"message\":\"bad key\"}"));
            props.getGovService().setBaseUrl(server.url("/api").toString());
            GovServiceApiClient client = new GovServiceApiClient(new ExternalPolicyApiClient(props), props);

            assertThatThrownBy(() -> client.fetchServiceList(1, 1))
                    .isInstanceOf(PolicyApiResponseException.class)
                    .hasMessageContaining("HTTP 403");
        }
    }

    private PolicyCollectionProperties props() {
        PolicyCollectionProperties props = new PolicyCollectionProperties();
        props.setConnectTimeout(Duration.ofSeconds(1));
        props.setReadTimeout(Duration.ofSeconds(1));
        props.setMaxRetries(1);
        props.getGovService().setBaseUrl("https://api.odcloud.kr/api");
        props.getGovService().setListPath("/gov24/v3/serviceList");
        props.getGovService().setDetailPath("/gov24/v3/serviceDetail");
        props.getGovService().setSupportConditionsPath("/gov24/v3/supportConditions");
        props.getGovService().setApiKey("test-key");
        props.getLocalWelfare().setBaseUrl("https://apis.data.go.kr/B554287/LocalGovernmentWelfareInformations");
        props.getLocalWelfare().setListPath("/LcgvWelfarelist");
        props.getLocalWelfare().setDetailPath("/LcgvWelfaredetailed");
        props.getLocalWelfare().setApiKey("test-key");
        props.getCentralWelfare().setBaseUrl("https://apis.data.go.kr/B554287/NationalWelfareInformationsV001");
        props.getCentralWelfare().setListPath("/NationalWelfarelistV001");
        props.getCentralWelfare().setDetailPath("/NationalWelfaredetailedV001");
        props.getCentralWelfare().setApiKey("test-key");
        return props;
    }
}
