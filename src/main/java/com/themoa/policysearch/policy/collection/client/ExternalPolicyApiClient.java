package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExternalPolicyApiClient {
    private static final Pattern ENCODED_OCTET = Pattern.compile("%[0-9a-fA-F]{2}");
    private final RestClient restClient;
    private final PolicyCollectionProperties properties;

    public ExternalPolicyApiClient(PolicyCollectionProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public ExternalApiResponse getJson(PolicySource source, String baseUrl, String path, Map<String, ?> queryParameters) {
        return get(source, baseUrl, path, queryParameters, MediaType.APPLICATION_JSON_VALUE);
    }

    public ExternalApiResponse getXml(PolicySource source, String baseUrl, String path, Map<String, ?> queryParameters) {
        return get(source, baseUrl, path, queryParameters, MediaType.APPLICATION_XML_VALUE + ", text/xml");
    }

    public ExternalApiResponse get(PolicySource source, String baseUrl, String path, Map<String, ?> queryParameters,
                                   String acceptHeader) {
        URI uri = buildUri(baseUrl, path, queryParameters);
        String maskedUrl = maskKey(uri.toString());
        int attempts = Math.max(1, properties.getMaxRetries());
        Duration backoff = Duration.ofMillis(200);
        RuntimeException last = null;
        for (int i = 0; i < attempts; i++) {
            try {
                return restClient.get()
                        .uri(uri)
                        .header(HttpHeaders.ACCEPT, acceptHeader)
                        .exchange((request, response) -> {
                            String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                            String contentType = response.getHeaders().getContentType() == null
                                    ? null
                                    : response.getHeaders().getContentType().toString();
                            ExternalApiResponse apiResponse = new ExternalApiResponse(
                                    response.getStatusCode().value(), contentType, body, maskedUrl);
                            if (response.getStatusCode().isError() || response.getStatusCode().is3xxRedirection()) {
                                throw new PolicyApiResponseException(source, apiResponse.statusCode(),
                                        apiResponse.contentType(), null, "HTTP " + apiResponse.statusCode(),
                                        apiResponse.maskedRequestUrl(), apiResponse.body());
                            }
                            return apiResponse;
                        });
            } catch (RuntimeException ex) {
                last = ex;
                if (i + 1 < attempts) {
                    sleep(backoff.multipliedBy(1L << i));
                }
            }
        }
        if (last instanceof PolicyApiException policyApiException) {
            throw policyApiException;
        }
        throw new PolicyApiException("외부 정책 API 호출에 실패했습니다. requestUrl=" + maskedUrl
                + ", cause=" + (last == null ? "unknown" : last.getMessage()), last);
    }

    public String requestUrl(String baseUrl, String path, Map<String, ?> queryParameters) {
        return maskKey(buildUri(baseUrl, path, queryParameters).toString());
    }

    public URI buildUri(String baseUrl, String path, Map<String, ?> queryParameters) {
        StringBuilder builder = new StringBuilder(stripTrailingSlash(baseUrl)).append(normalizePath(path));
        if (!queryParameters.isEmpty()) {
            builder.append('?');
            boolean first = true;
            for (Map.Entry<String, ?> entry : queryParameters.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                if (!first) {
                    builder.append('&');
                }
                first = false;
                builder.append(encode(entry.getKey()))
                        .append('=')
                        .append(encodeQueryValue(String.valueOf(entry.getValue())));
            }
        }
        return URI.create(builder.toString());
    }

    public String maskKey(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("(?i)(serviceKey|openApiVlak|apiKey)=([^&\\s]+)", "$1=****");
    }

    private String encodeQueryValue(String value) {
        if (ENCODED_OCTET.matcher(value).find()) {
            return value;
        }
        return encode(value);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            throw new PolicyApiException("API base-url이 설정되지 않았습니다.");
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            throw new PolicyApiException("해당 출처의 API endpoint가 설정되지 않았습니다. 공공데이터포털 Swagger에서 목록조회 URL을 확인해 설정하세요.");
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PolicyApiException("외부 API 재시도 대기가 중단되었습니다.", ex);
        }
    }
}
