package me.indian.broadcast.core.models;

public record FriendModifyResponse(
        int code,
        String description,
        String source,
        Object traceInformation
) {
}
