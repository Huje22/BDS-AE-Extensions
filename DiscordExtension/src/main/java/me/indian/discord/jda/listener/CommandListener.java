package me.indian.discord.jda.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.stats.StatsManager;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.watchdog.module.BackupModule;
import me.indian.bds.watchdog.module.pack.PackModule;
import me.indian.discord.DiscordExtension;
import me.indian.discord.config.DiscordConfig;
import me.indian.discord.config.sub.BotConfig;
import me.indian.discord.jda.DiscordJDA;
import me.indian.discord.jda.manager.LinkingManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter implements JDAListener {

    private final DiscordExtension discordExtension;
    private final DiscordJDA discordJDA;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final DiscordConfig discordConfig;
    private final BotConfig botConfig;

    private final List<String> allowlistPlayers;
    private final ExecutorService service;
    private final ServerProcess serverProcess;
    private JDA jda;
    private StatsManager statsManager;
    private BackupModule backupModule;
    private PackModule packModule;
    private LinkingManager linkingManager;

    public CommandListener( final DiscordExtension discordExtension) {
        this.discordExtension = discordExtension;
        this.discordJDA = this.discordExtension.getDiscordJDA();
        this.bdsAutoEnable = discordExtension.getBdsAutoEnable();
        this.logger = discordExtension.getLogger();
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.discordConfig = discordExtension.getConfig();
        this.botConfig = this.discordConfig.getBotConfig();
        this.allowlistPlayers = new LinkedList<>();
        this.service = Executors.newScheduledThreadPool(3, new ThreadUtil("Discord Command Listener"));
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
    }

    @Override
    public void init() {
        this.jda = this.discordJDA.getJda();
        this.statsManager = this.bdsAutoEnable.getServerManager().getStatsManager();
        this.backupModule = this.bdsAutoEnable.getWatchDog().getBackupModule();
        this.packModule = this.bdsAutoEnable.getWatchDog().getPackModule();
        this.linkingManager = this.discordJDA.getLinkingManager();
    }







}
