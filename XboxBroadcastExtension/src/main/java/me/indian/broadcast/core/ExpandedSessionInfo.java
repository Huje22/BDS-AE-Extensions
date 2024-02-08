package me.indian.broadcast.core;

import java.util.Random;
import java.util.UUID;

public class ExpandedSessionInfo extends SessionInfo {
    private String connectionId;
    private String xuid;
    private String rakNetGUID;
    private String sessionId;
    private String handleId;

    public ExpandedSessionInfo(final String connectionId, final String xuid, final SessionInfo sessionInfo) {
        this.connectionId = connectionId;
        this.xuid = xuid;

        final StringBuilder str = new StringBuilder();
        final Random random = new Random();
        for (int i = 0; i < 20; i++) {
            str.append(random.nextInt(10));
        }
        this.rakNetGUID = str.toString();

        this.sessionId = UUID.randomUUID().toString();

        this.setHostName(sessionInfo.getHostName());
        this.setWorldName(sessionInfo.getWorldName());
        this.setVersion(sessionInfo.getVersion());
        this.setProtocol(sessionInfo.getProtocol());
        this.setPlayers(sessionInfo.getPlayers());
        this.setMaxPlayers(sessionInfo.getMaxPlayers());
        this.setIp(sessionInfo.getIp());
        this.setPort(sessionInfo.getPort());
    }

    public void updateSessionInfo(final SessionInfo sessionInfo) {
        this.setHostName(sessionInfo.getHostName());
        this.setWorldName(sessionInfo.getWorldName().isEmpty() ? sessionInfo.getHostName() : sessionInfo.getWorldName());
        this.setVersion(sessionInfo.getVersion());
        this.setProtocol(sessionInfo.getProtocol());
        this.setPlayers(sessionInfo.getPlayers());
        this.setMaxPlayers(sessionInfo.getMaxPlayers());
        this.setIp(sessionInfo.getIp());
        this.setPort(sessionInfo.getPort());
    }

    public String getConnectionId() {
        return this.connectionId;
    }

    public void setConnectionId(final String connectionId) {
        this.connectionId = connectionId;
    }

    public String getXuid() {
        return this.xuid;
    }

    public void setXuid(final String xuid) {
        this.xuid = xuid;
    }

    public String getRakNetGUID() {
        return this.rakNetGUID;
    }

    public void setRakNetGUID(final String rakNetGUID) {
        this.rakNetGUID = rakNetGUID;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    public String getHandleId() {
        return this.handleId;
    }

    public void setHandleId(final String handleId) {
        this.handleId = handleId;
    }
}
