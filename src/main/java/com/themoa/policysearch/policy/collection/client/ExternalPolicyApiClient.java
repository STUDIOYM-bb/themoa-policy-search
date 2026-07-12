package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ExternalPolicyApiClient {
    private final RestClient restClient;
    private final PolicyCollectionProperties properties;

    public ExternalPolicyApiClient(PolicyCollectionProperties properties) {
        this.properties = properties;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(properties.getReadTimeout());
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public String get(String baseUrl, String path, Map<String, ?> queryParameters) {
        URI uri = buildUri(baseUrl, path, queryParameters);
        int attempts = Math.max(1, properties.getMaxRetries());
        Duration backoff = Duration.ofMillis(200);
        RuntimeException last = null;
        for (int i = 0; i < attempts; i++) {
            try {
                return restClient.get()
                        .uri(uri)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, (request, response) -> {
                            throw new PolicyApiException("외부 정책 API 오류: HTTP " + response.getStatusCode().value());
                        })
                        .body(String.class);
            } catch (RuntimeException ex) {
                last = ex;
                if (i + 1 < attempts) {
                    sleep(backoff.multipliedBy(1L << i));
                }
            }
        }
        throw new PolicyApiException("외부 정책 API 호출에 실패했습니다.", last);
    }

    public String requestUrl(String baseUrl, String path, Map<String, ?> queryParameters) {
        return maskKey(buildUri(baseUrl, path, queryParameters).toString());
    }

    private URI buildUri(String baseUrl, String path, Map<String, ?> queryParameters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(path);
        queryParameters.forEach(builder::queryParam);
        return builder.build(false).toUri();
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PolicyApiException("외부 API 재시도 대기가 중단되었습니다.", ex);
        }
    }

    private String maskKey(String url) {
        return url.replaceAll("(?i)(serviceKey|openApiVlak)=([^&]+)", "$1=****");
    }
}
