package me.indian.broadcast.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import me.indian.broadcast.core.SessionInfo;

public class SessionConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"UWAGA: Sesia aktualizuje się tylko wtedy gdy server jest włączony"})
    @Comment("The amount of time in seconds to update session information")
    @Comment("Warning: This can be no lower than 20 due to xbox rate limits")
    private int updateInterval = 30;

    @Comment({""})
    @Comment("Should we query the bedrock server to sync the session information")
    private boolean queryServer = true;

    @Comment({""})
    @Comment("The data to broadcast over xbox live, this is the default if querying is enabled")
    private SessionInfo sessionInfo = new SessionInfo();

    public int getUpdateInterval() {
        return this.updateInterval;
    }

    public boolean isQueryServer() {
        return this.queryServer;
    }

    public SessionInfo getSessionInfo() {
        return this.sessionInfo;
    }
}