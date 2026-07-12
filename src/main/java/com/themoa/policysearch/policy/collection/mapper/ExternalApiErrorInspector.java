package com.themoa.policysearch.policy.collection.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.themoa.policysearch.policy.collection.client.ExternalApiResponse;
import com.themoa.policysearch.policy.collection.client.PolicyApiParseException;
import com.themoa.policysearch.policy.collection.client.PolicyApiResponseException;
import com.themoa.policysearch.policy.domain.PolicySource;
import org.springframework.stereotype.Component;

@Component
public class ExternalApiErrorInspector {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    public void assertUsable(PolicySource source, int page, String parserType, ExternalApiResponse response) {
        String body = response.body();
        if (body == null || body.isBlank()) {
            throw new PolicyApiParseException(source, page, parserType, response.contentType(), body,
                    response.maskedRequestUrl(), "외부 API가 빈 응답을 반환했습니다.");
        }
        if (looksHtml(body)) {
            throw new PolicyApiParseException(source, page, parserType, response.contentType(), body,
                    response.maskedRequestUrl(), "예상한 JSON/XML 대신 HTML 응답을 받았습니다.");
        }
        ApiError error = "XML".equalsIgnoreCase(parserType) ? xmlError(body) : jsonError(body);
        if (error != null && error.isError()) {
            throw new PolicyApiResponseException(source, response.statusCode(), response.contentType(),
                    error.code(), error.message(), response.maskedRequestUrl(), response.body());
        }
    }

    public ApiError detectError(String parserType, String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        return "XML".equalsIgnoreCase(parserType) ? xmlError(body) : jsonError(body);
    }

    private ApiError jsonError(String body) {
        try {
            JsonNode root = jsonMapper.readTree(body);
            String code = text(root, "resultCode");
            String message = text(root, "resultMsg");
            if (message == null) {
                message = text(root, "message");
            }
            if (code == null) {
                code = text(root, "error");
            }
            if (code != null || message != null) {
                boolean ok = code == null || "00".equals(code) || "0".equals(code) || "SUCCESS".equalsIgnoreCase(code);
                return new ApiError(code, message, !ok);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private ApiError xmlError(String body) {
        try {
            JsonNode root = xmlMapper.readTree(body);
            JsonNode header = child(root, "header");
            if (header != null) {
                String code = text(header, "resultCode");
                String message = text(header, "resultMsg");
                if (code != null || message != null) {
                    boolean ok = code == null || "00".equals(code) || "0".equals(code) || "NORMAL_CODE".equalsIgnoreCase(code);
                    return new ApiError(code, message, !ok);
                }
            }
            JsonNode cmm = child(root, "cmmMsgHeader");
            if (cmm != null) {
                String code = text(cmm, "returnReasonCode");
                String message = text(cmm, "returnAuthMsg");
                if (message == null) {
                    message = text(cmm, "errMsg");
                }
                return new ApiError(code, message, true);
            }
            String code = text(root, "resultCode");
            String message = text(root, "resultMessage");
            if (code != null || message != null) {
                boolean ok = code == null || "00".equals(code) || "0".equals(code) || "NORMAL_CODE".equalsIgnoreCase(code);
                return new ApiError(code, message, !ok);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private JsonNode child(JsonNode node, String name) {
        if (node == null) {
            return null;
        }
        JsonNode direct = node.get(name);
        if (direct != null) {
            return direct;
        }
        JsonNode response = node.get("response");
        if (response != null) {
            return response.get(name);
        }
        JsonNode openApi = node.get("OpenAPI_ServiceResponse");
        if (openApi != null) {
            return openApi.get(name);
        }
        return null;
    }

    private String text(JsonNode node, String name) {
        JsonNode value = node == null ? null : node.get(name);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text.trim();
    }

    private boolean looksHtml(String body) {
        String trimmed = body.stripLeading().toLowerCase();
        return trimmed.startsWith("<!doctype html") || trimmed.startsWith("<html");
    }

    public record ApiError(String code, String message, boolean isError) {
    }
}
