package me.indian.broadcast.core.models.session;

public record CreateHandleResponse(
        String createTime,
        Object gameTypes,
        String id,
        String inviteProtocol,
        String ownerXuid,
        SessionRef sessionRef,
        String titleId,
        String type,
        int version
) {
}
