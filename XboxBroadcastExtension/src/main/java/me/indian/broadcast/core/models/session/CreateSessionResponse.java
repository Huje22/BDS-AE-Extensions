package me.indian.broadcast.core.models.session;

import java.util.Map;
import me.indian.broadcast.core.models.session.member.SessionMember;

public record CreateSessionResponse(
        String branch,
        int changeNumber,
        Object constants,
        int contractVersion,
        String correlationId,
        Map<String, SessionMember> members,
        Object membersInfo,
        SessionProperties properties,
        Object servers,
        String startTime
) {
}
