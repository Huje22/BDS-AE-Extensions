package me.indian.broadcast;


import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.BedrockQuery;
import me.indian.broadcast.config.ExtensionConfig;
import me.indian.broadcast.core.SessionInfo;
import me.indian.broadcast.core.SessionManager;
import me.indian.broadcast.core.exceptions.SessionCreationException;
import me.indian.broadcast.core.exceptions.SessionUpdateException;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class XboxBroadcastExtension extends Extension {

    private BDSAutoEnable bdsAutoEnable;
    private ExtensionConfig config;
    private Logger logger;
    private SessionInfo sessionInfo;
    public SessionManager sessionManager;

    @Override
    public void onEnable() {
        this.bdsAutoEnable = this.getBdsAutoEnable();
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.createConfig(ExtensionConfig.class, "config");
        this.sessionManager = new SessionManager(this.getDataFolder() + File.separator + "cache", this.logger);
        this.sessionInfo = this.config.getSession().getSessionInfo();

        System.out.println(this.sessionInfo);

        this.updateSessionInfo(this.sessionInfo);

        //TODO: Dodaj komendy i dodaj "launch on ServerStart" czy cos takiego

        try {
            this.createSession();
        } catch (final Exception exception) {
            exception.printStackTrace();
        }

    }

    public void restart() {
        try {
            this.sessionManager.shutdown();
            this.sessionManager = new SessionManager("./cache", this.logger);
            this.createSession();
        } catch (final SessionCreationException | SessionUpdateException exception) {
            this.logger.error("Failed to restart session", exception);
        }
    }

    private void createSession() throws SessionCreationException, SessionUpdateException {
        this.sessionManager.restartCallback(this::restart);
        this.sessionManager.init(this.sessionInfo, this.config.getFriendSync());

        this.sessionManager.scheduledThread().scheduleWithFixedDelay(() -> {
            if (this.bdsAutoEnable.getServerProcess().isEnabled()) {
                this.updateSessionInfo(this.sessionInfo);

                try {
                    // Update the session
                    this.sessionManager.updateSession(this.sessionInfo);
                    this.sessionManager.logger().info("Updated session!");
                } catch (final SessionUpdateException exception) {
                    this.sessionManager.logger().error("Failed to update session", exception);
                }
            }
        }, this.config.getSession().getUpdateInterval(), this.config.getSession().getUpdateInterval(), TimeUnit.SECONDS);
    }

    private void updateSessionInfo(final SessionInfo sessionInfo) {
        if (this.config.getSession().isQueryServer()) {
            System.out.println(this.sessionInfo);

            final BedrockQuery query = BedrockQuery.create(sessionInfo.getIp(), sessionInfo.getPort());

            if (query.online()) {
                sessionInfo.setHostName(query.motd());
                sessionInfo.setWorldName(query.mapName());
                sessionInfo.setVersion(query.minecraftVersion());
                sessionInfo.setProtocol(query.protocol());
                sessionInfo.setPlayers(query.playerCount());
                sessionInfo.setMaxPlayers(query.maxPlayers());
            }
        }
    }
}