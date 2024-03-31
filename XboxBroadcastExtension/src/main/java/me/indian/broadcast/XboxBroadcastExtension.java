package me.indian.broadcast;


import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.CommandManager;
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.properties.ServerProperties;
import me.indian.bds.util.BedrockQuery;
import me.indian.bds.util.ThreadUtil;
import me.indian.broadcast.command.XboxBroadcastCommand;
import me.indian.broadcast.config.ExtensionConfig;
import me.indian.broadcast.config.SessionConfig;
import me.indian.broadcast.core.SessionInfo;
import me.indian.broadcast.core.SessionManager;
import me.indian.broadcast.core.exceptions.SessionCreationException;
import me.indian.broadcast.core.exceptions.SessionUpdateException;

public class XboxBroadcastExtension extends Extension {

    private BDSAutoEnable bdsAutoEnable;
    private String cacheLocation;
    private ExtensionConfig config;
    private Logger logger;
    private ExecutorService service;
    private SessionManager sessionManager;
    private SessionConfig sessionConfig;
    private SessionInfo sessionInfo;
    private List<String> randomWorldNames, randomHostNames;
    private Random random;

    @Override
    public void onEnable() {
        this.bdsAutoEnable = this.getBdsAutoEnable();
        this.cacheLocation = this.getDataFolder() + File.separator + "cache";
        this.logger = this.getLogger();
        this.config = this.createConfig(ExtensionConfig.class, "config");
        this.service = Executors.newScheduledThreadPool(2, new ThreadUtil("XboxBroadcastExtension"));
        this.sessionManager = new SessionManager(this.cacheLocation, this.service, this.logger);
        this.sessionConfig = this.config.getSession();
        this.sessionInfo = this.sessionConfig.getSessionInfo();
        this.randomHostNames = this.sessionConfig.getRandomHostNames();
        this.randomWorldNames = this.sessionConfig.getRandomWorldNames();
        this.random = new Random();


        this.updateSessionInfo(this.sessionInfo);

        final CommandManager commandManager = this.bdsAutoEnable.getCommandManager();
        commandManager.registerCommand(new XboxBroadcastCommand(this), this);

        this.service.execute(() -> {
            try {
                this.createSession();
            } catch (final Exception exception) {
                this.logger.error("Nie udało się utworzyć sesji na starcie", exception);
            }
        });
    }

    @Override
    public void onDisable() {
        this.sessionManager.shutdown();
    }

    public void restart() {
        this.service.execute(() -> {
            try {
                this.sessionManager.shutdown();
                this.sessionManager = new SessionManager(this.cacheLocation, this.service, this.logger);
                this.createSession();
            } catch (final SessionCreationException | SessionUpdateException exception) {
                this.logger.error("Failed to restart session", exception);
            }
        });
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
                    this.sessionManager.logger().info("&aZaktualizowano sesje!");


                } catch (final SessionUpdateException exception) {
                    this.sessionManager.logger().error("&cNie udało się zaktualizować sesji", exception);
                }
            }
        }, this.config.getSession().getUpdateInterval(), this.config.getSession().getUpdateInterval(), TimeUnit.SECONDS);
    }

    private void updateSessionInfo(final SessionInfo sessionInfo) {
        if (this.config.getSession().isQueryServer()) {
            final BedrockQuery query = BedrockQuery.create(sessionInfo.getIp(), sessionInfo.getPort());

            if (query.online()) {
                this.updateHostName(sessionInfo, query.motd());
                this.updateWorldName(sessionInfo, query.mapName());

                sessionInfo.setVersion(query.minecraftVersion());
                sessionInfo.setProtocol(query.protocol());
                sessionInfo.setPlayers(query.playerCount());
                sessionInfo.setMaxPlayers(query.maxPlayers());
            }
        } else {
            final ServerProperties serverProperties = this.bdsAutoEnable.getServerProperties();

            this.updateHostName(sessionInfo, serverProperties.getRealWorldName());
            this.updateWorldName(sessionInfo, serverProperties.getMOTD());

            sessionInfo.setVersion(this.bdsAutoEnable.getVersionManager().getLoadedVersion());
            sessionInfo.setProtocol(this.bdsAutoEnable.getVersionManager().getLastKnownProtocol());
            sessionInfo.setPlayers(this.bdsAutoEnable.getServerManager().getOnlinePlayers().size());
            sessionInfo.setMaxPlayers(serverProperties.getMaxPlayers());
        }
        System.out.println(sessionInfo);
    }


    private void updateHostName(final SessionInfo sessionInfo, final String hostName) {
        if (!this.sessionConfig.isCustomHostName()) {
            sessionInfo.setHostName(hostName);
        } else {
            if (this.sessionConfig.isRandomHostName()) {
                sessionInfo.setHostName(this.randomHostNames.get(this.random.nextInt(this.randomHostNames.size())));
            }
        }
    }

    private void updateWorldName(final SessionInfo sessionInfo, final String worldName) {
        if (!this.sessionConfig.isCustomWorldName()) {
            sessionInfo.setWorldName(worldName);
        } else {
            if (this.sessionConfig.isRandomWorldName()) {
                sessionInfo.setWorldName(this.randomWorldNames.get(this.random.nextInt(this.randomWorldNames.size())));
            }
        }
    }

    public String getCacheLocation() {
        return this.cacheLocation;
    }

    public ExtensionConfig getConfig() {
        return this.config;
    }

    public SessionInfo getSessionInfo() {
        return this.sessionInfo;
    }

    public SessionManager getSessionManager() {
        return this.sessionManager;
    }
}