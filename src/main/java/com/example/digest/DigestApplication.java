package com.example.digest;

public final class DigestApplication {
    public static void main(String[] args) throws Exception {
        LayeredConfig config = LayeredConfig.fromEnvironment();
        WeeklyDigestService service = new WeeklyDigestService(new InfraiClient(config.apiKey()), config);
        System.out.println("scheduled weekly digest: " + service.schedule());
        System.out.println(WeeklyDigestService.renderReceipt("ORD-1042", "Ava Chen", 3));
    }
}
