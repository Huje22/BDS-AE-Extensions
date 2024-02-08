package me.indian.broadcast.core.models.auth;

public record SISUAuthenticationResponse(
        GenericAuthenticationResponse AuthorizationToken,
        String DeviceToken,
        String Sandbox,
        GenericAuthenticationResponse TitleToken,
        boolean UseModernGamertag,
        GenericAuthenticationResponse UserToken,
        String WebPage
) {
}
