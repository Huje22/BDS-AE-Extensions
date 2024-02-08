package me.indian.broadcast.core.models.session;

public record SessionProperties(
        SessionSystemProperties system,
        SessionCustomProperties custom
) {
}
