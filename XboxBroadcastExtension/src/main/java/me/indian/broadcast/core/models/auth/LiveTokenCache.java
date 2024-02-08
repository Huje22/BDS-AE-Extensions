package me.indian.broadcast.core.models.auth;

public class LiveTokenCache {
    public long obtainedOn;
    public LiveTokenResponse token;

    public LiveTokenCache() {
    }

    public LiveTokenCache(final long obtainedOn, final LiveTokenResponse token) {
        this.obtainedOn = obtainedOn;
        this.token = token;
    }
}
