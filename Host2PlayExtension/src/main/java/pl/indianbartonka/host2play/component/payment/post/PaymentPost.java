package pl.indianbartonka.host2play.component.payment.post;

public record PaymentPost(
        String customerEmail,
        int amount,
        String currency,
        String notificationUrl,
        String successRedirectUrl,
        String cancelRedirectUrl,
        String description,
        boolean autoRedirect
) {
}
