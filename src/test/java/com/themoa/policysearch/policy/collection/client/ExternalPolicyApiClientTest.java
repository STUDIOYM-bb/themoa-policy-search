package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalPolicyApiClientTest {
    @Test
    void masksApiKeysInUrls() {
        ExternalPolicyApiClient client = client();

        String masked = client.maskKey("https://example.test?a=1&serviceKey=abc%2Bdef&openApiVlak=raw-key");

        assertThat(masked).contains("serviceKey=****");
        assertThat(masked).contains("openApiVlak=****");
        assertThat(masked).doesNotContain("abc%2Bdef");
        assertThat(masked).doesNotContain("raw-key");
    }

    @Test
    void doesNotDoubleEncodeDataGoEncodingKey() {
        ExternalPolicyApiClient client = client();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("serviceKey", "abc%2Bdef%2Fghi%3D");

        URI uri = client.buildUri("https://apis.data.go.kr", "/test", params);

        assertThat(uri.toString()).contains("serviceKey=abc%2Bdef%2Fghi%3D");
        assertThat(uri.toString()).doesNotContain("%252B");
    }

    @Test
    void encodesDecodingKeyWhenNeeded() {
        ExternalPolicyApiClient client = client();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("serviceKey", "abc+def/ghi=");

        URI uri = client.buildUri("https://apis.data.go.kr", "/test", params);

        assertThat(uri.toString()).contains("serviceKey=abc%2Bdef%2Fghi%3D");
    }

    @Test
    void emptyKeyIsKeptEmpty() {
        ExternalPolicyApiClient client = client();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("serviceKey", "");

        URI uri = client.buildUri("https://apis.data.go.kr", "/test", params);

        assertThat(uri.toString()).endsWith("serviceKey=");
    }

    private ExternalPolicyApiClient client() {
        PolicyCollectionProperties props = new PolicyCollectionProperties();
        props.setConnectTimeout(Duration.ofSeconds(1));
        props.setReadTimeout(Duration.ofSeconds(1));
        props.setMaxRetries(1);
        return new ExternalPolicyApiClient(props);
    }
}
