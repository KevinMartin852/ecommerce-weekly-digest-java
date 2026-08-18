package com.example.digest;

public record LayeredConfig(String apiKey, String webhookUrl, String cronExpr) {
    public static LayeredConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        String webhook = System.getenv().getOrDefault("DIGEST_WEBHOOK_URL", "https://example.com/webhooks/weekly-digest");
        String cron = System.getenv().getOrDefault("DIGEST_CRON", "0 9 * * 1");
        return new LayeredConfig(key, webhook, cron);
    }
}
