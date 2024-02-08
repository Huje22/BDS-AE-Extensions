package me.indian.broadcast.core.models;

import java.time.Instant;

public record FriendStatusResponse(
        String xuid,
        Instant addedDateTimeUtc,
        boolean isFavorite,
        Object[] socialNetworks,
        boolean isFollowedByCaller,
        boolean isFollowingCaller,
        boolean isIdentityShared,
        boolean isSquadMateWith,
        boolean isUnfollowingFeed
) {
}
