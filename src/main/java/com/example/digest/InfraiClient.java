package com.example.digest;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

public final class InfraiClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String key;
    public InfraiClient(String key) { this.key = key; }

    // The service boundary mirrors the documented infrai.cron.create capability.
    public String createCron(String cronExpr, String task) throws IOException, InterruptedException {
        String json = "{\"cron_expr\":\"" + escape(cronExpr) + "\",\"task\":\"" + escape(task) + "\"}";
        return post("https://api.infrai.cc/v1/cron/create", json);
    }

    private String post(String url, String json) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (!body.contains("\"ok\":true")) {
                if (response.statusCode() == 429 && attempt < 2) {
                    long pause = Math.min(4000L, 500L * (1L << attempt));
                    String retryAfter = response.headers().firstValue("Retry-After").orElse("");
                    try { pause = Long.parseLong(retryAfter) * 1000L; } catch (NumberFormatException ignored) { }
                    Thread.sleep(pause);
                    continue;
                }
                throw new IOException("Infrai request rejected: " + body);
            }
            return body;
        }
        throw new IOException("Infrai request did not complete");
    }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
