package me.indian.host2play.component;

public record PaymentPostData(
        String customerEmail,
        int amount,
        String currency,
        String notificationUrl,
        String successRedirectUrl,
        String cancelRedirectUrl,
        String description,
        boolean autoRedirect
) {}
