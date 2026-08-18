package com.example.digest;

public final class WeeklyDigestServiceTest {
    public static void main(String[] args) {
        String receipt = WeeklyDigestService.renderReceipt("ORD-7", "Mina", 2);
        if (!receipt.equals("Receipt ORD-7 for Mina (2 items)")) throw new AssertionError(receipt);
        try { WeeklyDigestService.renderReceipt("", "Mina", 2); throw new AssertionError("blank order accepted"); }
        catch (IllegalArgumentException expected) { }
        System.out.println("WeeklyDigestServiceTest passed");
    }
}
