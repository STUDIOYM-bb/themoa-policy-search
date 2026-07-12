package com.themoa.policysearch.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminKeyValidator {
    private final String adminApiKey;

    public AdminKeyValidator(@Value("${app.admin-api-key:}") String adminApiKey) {
        this.adminApiKey = adminApiKey;
    }

    public boolean isValid(String candidate) {
        return StringUtils.hasText(adminApiKey) && adminApiKey.equals(candidate);
    }
}
