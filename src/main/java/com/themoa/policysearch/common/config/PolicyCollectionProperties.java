package com.themoa.policysearch.common.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.policy.collection")
public class PolicyCollectionProperties {
    private boolean enabled;
    private String cron = "0 0 3 * * *";
    private int pageSize = 100;
    private int maxPages = 1000;
    private Duration requestDelay = Duration.ofMillis(500);
    private Duration detailRequestDelay = Duration.ofMillis(100);
    private int detailMaxConcurrency = 3;
    private int maxRetries = 3;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);
    private SourceProperties youthCenter = new SourceProperties();
    private SourceProperties govService = new SourceProperties();
    private SourceProperties localWelfare = new SourceProperties();
    private SourceProperties centralWelfare = new SourceProperties();
    private SourcesProperties sources = new SourcesProperties();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getMaxPages() { return maxPages; }
    public void setMaxPages(int maxPages) { this.maxPages = maxPages; }
    public Duration getRequestDelay() { return requestDelay; }
    public void setRequestDelay(Duration requestDelay) { this.requestDelay = requestDelay; }
    public Duration getDetailRequestDelay() { return detailRequestDelay; }
    public void setDetailRequestDelay(Duration detailRequestDelay) { this.detailRequestDelay = detailRequestDelay; }
    public int getDetailMaxConcurrency() { return detailMaxConcurrency; }
    public void setDetailMaxConcurrency(int detailMaxConcurrency) { this.detailMaxConcurrency = detailMaxConcurrency; }
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
    public SourcesProperties getSources() { return sources; }
    public void setSources(SourcesProperties sources) { this.sources = sources; }

    public static class SourcesProperties {
        private boolean youthCenterEnabled = false;
        private boolean govServiceEnabled = true;
        private boolean localWelfareEnabled = true;
        private boolean centralWelfareEnabled = true;

        public boolean isYouthCenterEnabled() { return youthCenterEnabled; }
        public void setYouthCenterEnabled(boolean youthCenterEnabled) { this.youthCenterEnabled = youthCenterEnabled; }
        public boolean isGovServiceEnabled() { return govServiceEnabled; }
        public void setGovServiceEnabled(boolean govServiceEnabled) { this.govServiceEnabled = govServiceEnabled; }
        public boolean isLocalWelfareEnabled() { return localWelfareEnabled; }
        public void setLocalWelfareEnabled(boolean localWelfareEnabled) { this.localWelfareEnabled = localWelfareEnabled; }
        public boolean isCentralWelfareEnabled() { return centralWelfareEnabled; }
        public void setCentralWelfareEnabled(boolean centralWelfareEnabled) { this.centralWelfareEnabled = centralWelfareEnabled; }
    }

    public static class SourceProperties {
        private String baseUrl;
        private String listPath;
        private String detailPath;
        private String supportConditionsPath;
        private ResponseFormat responseFormat = ResponseFormat.JSON;
        private String apiKey;
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getListPath() { return listPath; }
        public void setListPath(String listPath) { this.listPath = listPath; }
        public String getDetailPath() { return detailPath; }
        public void setDetailPath(String detailPath) { this.detailPath = detailPath; }
        public String getSupportConditionsPath() { return supportConditionsPath; }
        public void setSupportConditionsPath(String supportConditionsPath) { this.supportConditionsPath = supportConditionsPath; }
        public ResponseFormat getResponseFormat() { return responseFormat; }
        public void setResponseFormat(ResponseFormat responseFormat) { this.responseFormat = responseFormat; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public void requireBaseListDetail(String sourceName) {
            requireText(baseUrl, sourceName + " base-url이 설정되지 않았습니다.");
            requireText(listPath, sourceName + " list-path가 설정되지 않았습니다.");
            requireText(detailPath, sourceName + " detail-path가 설정되지 않았습니다.");
            requireText(apiKey, sourceName + " API 키가 설정되지 않았습니다.");
        }
        public void requireSupportConditionsPath(String sourceName) {
            requireText(supportConditionsPath, sourceName + " support-conditions-path가 설정되지 않았습니다.");
        }
        private void requireText(String value, String message) {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException(message);
            }
        }
    }

    public enum ResponseFormat {
        JSON,
        XML
    }
}
