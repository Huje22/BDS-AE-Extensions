package me.indian.broadcast.core.models.session;

public record SessionSystemProperties(
        String joinRestriction,
        String readRestriction,
        boolean closed
) {
    public SessionSystemProperties() {
        this("followed", "followed", false);
    }
}
