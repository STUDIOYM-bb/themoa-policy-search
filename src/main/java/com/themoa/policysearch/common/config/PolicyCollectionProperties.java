package com.themoa.policysearch.common.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.policy.collection")
public class PolicyCollectionProperties {
    private boolean enabled;
    private String cron = "0 0 3 * * *";
    private int pageSize = 100;
    private Duration requestDelay = Duration.ofMillis(500);
    private int maxRetries = 3;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);
    private SourceProperties youthCenter = new SourceProperties();
    private SourceProperties govService = new SourceProperties();
    private SourceProperties localWelfare = new SourceProperties();
    private SourceProperties centralWelfare = new SourceProperties();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public Duration getRequestDelay() { return requestDelay; }
    public void setRequestDelay(Duration requestDelay) { this.requestDelay = requestDelay; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
    public SourceProperties getYouthCenter() { return youthCenter; }
    public SourceProperties getGovService() { return govService; }
    public SourceProperties getLocalWelfare() { return localWelfare; }
    public SourceProperties getCentralWelfare() { return centralWelfare; }

    public static class SourceProperties {
        private String baseUrl;
        private String apiKey;
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }
}
