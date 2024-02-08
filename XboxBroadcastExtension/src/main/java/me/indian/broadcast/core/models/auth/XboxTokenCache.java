package me.indian.broadcast.core.models.auth;

public record XboxTokenCache(XboxTokenInfo xstsToken) {
    public XboxTokenCache() {
        this(null);
    }
}