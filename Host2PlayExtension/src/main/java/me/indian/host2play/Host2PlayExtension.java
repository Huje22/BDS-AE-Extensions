package me.indian.host2play;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.HTTPUtil;
import me.indian.discord.DiscordExtension;
import me.indian.host2play.command.DonationCommand;
import me.indian.host2play.config.Config;
import me.indian.host2play.listener.ExtensionEnableListener;
import me.indian.host2play.rest.NotificationEndpoint;
import me.indian.host2play.util.RequestUtil;
import me.indian.rest.RestWebsite;
import org.jetbrains.annotations.Nullable;

public class Host2PlayExtension extends Extension {

    private BDSAutoEnable bdsAutoEnable;
    private Config config;
    private Logger logger;
    private String notificationEndpoint;
    private String fullNotificationEndpoint;
    private DiscordExtension discordExtension;

    @Override
    public void onLoad() {
        this.bdsAutoEnable = this.getBdsAutoEnable();
        this.logger = this.getLogger();
        this.config = this.createConfig(Config.class, "config");

        final String ip = this.config.getIp();
        if (this.config.isDynamicIP() || ip == null || ip.isEmpty()) {
            this.config.setIp(HTTPUtil.getOwnIP());
        }
    }

    @Override
    public void onEnable() {
        this.discordExtension = (DiscordExtension) this.bdsAutoEnable.getExtensionManager().getExtension("DiscordExtension");

        final RestWebsite restWebsite = (RestWebsite) this.bdsAutoEnable.getExtensionManager().getExtension("RestWebsite");

        if (restWebsite != null) {
            if (restWebsite.isEnabled()) {
                this.notificationEndpoint = "api/notification/" + this.config.getEndpointUUID();
                this.fullNotificationEndpoint = "http://" + this.config.getIp()
                        + ":" + restWebsite.getConfig().getPort() + "/" + this.notificationEndpoint;

                RequestUtil.init(this);

                if (RequestUtil.testKey()) {
                    //TODO: Dodać opcje włączenia komendy w config z opisem (ponieważ niedługo w opcji donate będzie można ustawic "NotifucationURL")
                    final DonationCommand donationCommand = new DonationCommand(this);
                    this.bdsAutoEnable.getCommandManager().registerCommand(donationCommand, this);
                    restWebsite.register(new NotificationEndpoint(this, donationCommand));
                } else {
                    this.bdsAutoEnable.getEventManager().registerListener(new ExtensionEnableListener(this), this);
                    this.logger.error("&cTwój klucz&b API&c jest nie poprawny!");
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (this.config != null) this.config.save();
    }

    public Config getConfig() {
        return this.config;
    }

    public String getNotificationEndpoint() {
        return this.notificationEndpoint;
    }

    public String getFullNotificationEndpoint() {
        return this.fullNotificationEndpoint;
    }

    @Nullable
    public DiscordExtension getDiscordExtension() {
        return this.discordExtension;
    }
}
