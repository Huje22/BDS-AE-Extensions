package me.indian.discord;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.CommandManager;
import me.indian.bds.event.EventManager;
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;
import me.indian.discord.command.DiscordCommand;
import me.indian.discord.command.LinkCommand;
import me.indian.discord.command.UnlinkCommand;
import me.indian.discord.config.DiscordConfig;
import me.indian.discord.config.MessagesConfig;
import me.indian.discord.jda.DiscordJDA;
import me.indian.discord.jda.manager.LinkingManager;
import me.indian.discord.jda.manager.StatsChannelsManager;
import me.indian.discord.listener.BackupListener;
import me.indian.discord.listener.PlayerEventListener;
import me.indian.discord.listener.ServerListener;
import me.indian.discord.webhook.WebHook;
import net.dv8tion.jda.api.JDA;

import java.util.concurrent.TimeUnit;

public class DiscordExtension extends Extension {

    private BDSAutoEnable bdsAutoEnable;
    private DiscordConfig config;
    private MessagesConfig messagesConfig;
    private Logger logger;
    private DiscordJDA discordJDA;
    private WebHook webHook;
    private boolean botEnabled, webhookEnabled;


    @Override
    public void onEnable() {
        this.bdsAutoEnable = this.getBdsAutoEnable();
        this.config = this.createConfig(DiscordConfig.class, "config");
        this.messagesConfig = this.createConfig(MessagesConfig.class, "Messages");

        this.logger = this.getLogger();

        this.botEnabled = this.getConfig().getBotConfig().isEnable();
        this.webhookEnabled = this.getConfig().getWebHookConfig().isEnable();

        this.discordJDA = new DiscordJDA(this);
        this.webHook = new WebHook(this);

        this.discordJDA.init();

        final CommandManager commandManager = this.bdsAutoEnable.getCommandManager();

        if (this.botEnabled) {
            commandManager.registerCommand(new DiscordCommand(this));
            commandManager.registerCommand(new LinkCommand(this));
            commandManager.registerCommand(new UnlinkCommand(this));
        }

        final EventManager eventManager = this.bdsAutoEnable.getEventManager();

        eventManager.registerListener(new BackupListener(this));
        eventManager.registerListener(new PlayerEventListener(this));
        eventManager.registerListener(new ServerListener(this));
    }

    @Override
    public void onDisable() {
        this.startShutdown();
        if (this.config != null) this.config.save();
        if(this.messagesConfig != null) this.messagesConfig.save();
        this.shutdown();
    }

    public DiscordConfig getConfig() {
        return this.config;
    }

    public MessagesConfig getMessagesConfig() {
        return this.messagesConfig;
    }

    private void startShutdown() {
        final LinkingManager linkingManager = this.discordJDA.getLinkingManager();
        final StatsChannelsManager statsChannelsManager = this.discordJDA.getStatsChannelsManager();

        if (linkingManager != null) linkingManager.saveLinkedAccounts();
        if (statsChannelsManager != null) statsChannelsManager.onShutdown();
    }

    private void shutdown() {
        final JDA jda = this.discordJDA.getJda();
        if (jda != null) {
            if (jda.getStatus() == JDA.Status.CONNECTED) {
                try {
                    jda.shutdown();
                    if (!jda.awaitShutdown(10L, TimeUnit.SECONDS)) {
                        this.logger.info("Wyłączono bota");
                    }
                } catch (final Exception exception) {
                    this.logger.critical("Nie można wyłączyć bota", exception);
                }
            }
        }

        this.webHook.shutdown();
    }

    public DiscordJDA getDiscordJDA() {
        return this.discordJDA;
    }

    public WebHook getWebHook() {
        return this.webHook;
    }

    public boolean isBotEnabled() {
        return this.botEnabled;
    }

    public void setBotEnabled(final boolean botEnabled) {
        this.botEnabled = botEnabled;
    }

    public boolean isWebhookEnabled() {
        return this.webhookEnabled;
    }

    public void setWebhookEnabled(final boolean webhookEnabled) {
        this.webhookEnabled = webhookEnabled;
    }
}