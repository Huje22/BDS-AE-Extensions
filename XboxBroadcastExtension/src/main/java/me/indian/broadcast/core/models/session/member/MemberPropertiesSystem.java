package me.indian.broadcast.core.models.session.member;

public record MemberPropertiesSystem(
        boolean active,
        String connection,
        MemberSubscription subscription
) {
}
