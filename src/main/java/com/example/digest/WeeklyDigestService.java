package com.example.digest;

public final class WeeklyDigestService {
    private final InfraiClient client;
    private final LayeredConfig config;
    public WeeklyDigestService(InfraiClient client, LayeredConfig config) { this.client = client; this.config = config; }

    public String schedule() throws Exception {
        return client.createCron(config.cronExpr(), config.webhookUrl());
    }

    public static String renderReceipt(String orderId, String customer, int itemCount) {
        if (orderId == null || orderId.isBlank() || customer == null || customer.isBlank() || itemCount < 1)
            throw new IllegalArgumentException("A receipt needs an order, customer, and at least one item");
        return "Receipt " + orderId + " for " + customer + " (" + itemCount + " items)";
    }
}
