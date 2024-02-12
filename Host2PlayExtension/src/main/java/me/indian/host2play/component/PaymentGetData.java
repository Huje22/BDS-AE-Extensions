package me.indian.host2play.component;

public record PaymentGetData(
        String customerEmail,
        int amount,
        String currency,
        String status,
        String notificationUrl,
        String successRedirectUrl,
        String cancelRedirectUrl,
        long created,
        long expires,
        String description
) {}
