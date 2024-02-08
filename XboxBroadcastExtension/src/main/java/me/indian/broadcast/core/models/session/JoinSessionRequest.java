package me.indian.broadcast.core.models.session;

import me.indian.broadcast.core.ExpandedSessionInfo;
import me.indian.broadcast.core.models.session.member.MemberConstantsSystem;
import me.indian.broadcast.core.models.session.member.MemberPropertiesSystem;
import me.indian.broadcast.core.models.session.member.MemberSubscription;
import me.indian.broadcast.core.models.session.member.SessionMember;

import java.util.HashMap;
import java.util.Map;

public class JoinSessionRequest {
    public Map<String, SessionMember> members;

    public JoinSessionRequest(final ExpandedSessionInfo sessionInfo) {
        final Map<String, MemberConstantsSystem> constants = new HashMap<>() {{
            this.put("system", new MemberConstantsSystem(sessionInfo.getXuid(), true));
        }};
        final Map<String, MemberPropertiesSystem> properties = new HashMap<>() {{
            this.put("system", new MemberPropertiesSystem(true, sessionInfo.getConnectionId(), new MemberSubscription()));
        }};

        this.members = new HashMap<>() {{
            this.put("me", new SessionMember(null, constants, null, properties));
        }};
    }
}
