package me.indian.broadcast.core;

import me.indian.bds.logger.Logger;
import me.indian.broadcast.core.exceptions.SessionUpdateException;
import me.indian.broadcast.core.models.session.JoinSessionRequest;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Simple manager to authenticate and create sessions on Xbox
 */
public class SubSessionManager extends SessionManagerCore {
    private final SessionManager parent;

    /**
     * Create a new session manager for a sub-session
     *
     * @param id     The id of the sub-session
     * @param parent The parent session manager
     * @param cache  The directory to store the cached tokens in
     * @param logger The logger to use for outputting messages
     */
    public SubSessionManager(final String id, final SessionManager parent, final String cache, final Logger logger) {
        super(cache, logger);
        this.parent = parent;
    }

    @Override
    public ScheduledExecutorService scheduledThread() {
        return this.parent.scheduledThread();
    }

    @Override
    public String getSessionId() {
        return this.parent.sessionInfo().getSessionId();
    }

    @Override
    protected boolean handleFriendship() {
        // TODO Some form of force flag just in case the master friends list is full

        // Add the main account
        final boolean subAdd = this.friendManager().addIfRequired(this.parent.getXboxToken().userXUID(), this.parent.getXboxToken().gamertag());

        // Get the main account to add us
        final boolean mainAdd = this.parent.friendManager().addIfRequired(this.getXboxToken().userXUID(), this.getXboxToken().gamertag());

        return subAdd || mainAdd;
    }

    @Override
    protected void updateSession() throws SessionUpdateException {
        super.updateSessionInternal(Constants.JOIN_SESSION.formatted(this.parent.sessionInfo().getHandleId()), new JoinSessionRequest(this.parent.sessionInfo()));
    }
}
