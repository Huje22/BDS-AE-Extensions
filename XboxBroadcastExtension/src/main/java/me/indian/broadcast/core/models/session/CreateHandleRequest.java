package me.indian.broadcast.core.models.session;

public class CreateHandleRequest {
    public int version;
    public String type;
    public SessionRef sessionRef;

    public CreateHandleRequest(final int version, final String type, final SessionRef sessionRef) {
        this.version = version;
        this.type = type;
        this.sessionRef = sessionRef;
    }
}
