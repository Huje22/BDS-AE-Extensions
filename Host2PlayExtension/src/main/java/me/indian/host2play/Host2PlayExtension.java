package me.indian.host2play;

import java.io.IOException;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;
import me.indian.discord.DiscordExtension;
import me.indian.host2play.command.DonationCommand;
import me.indian.host2play.config.Config;
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
    public void onEnable() {
        this.bdsAutoEnable = this.getBdsAutoEnable();
        this.logger = this.getLogger();
        this.config = this.createConfig(Config.class, "config");
        this.discordExtension = (DiscordExtension) this.bdsAutoEnable.getExtensionLoader().getExtension("DiscordExtension");

        final RestWebsite restWebsite = (RestWebsite) this.bdsAutoEnable.getExtensionLoader().getExtension("RestWebsite");

        if (restWebsite != null) {
            if (restWebsite.isEnabled()) {

                final String ip;
                try {
                    ip = RequestUtil.getOwnIP();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }

                this.notificationEndpoint = "api/notification/" + this.config.getEndpointUUID();
                this.fullNotificationEndpoint = "http://" + ip + ":" + restWebsite.getConfig().getPort() + "/" + this.notificationEndpoint;

                RequestUtil.init(this);


                if (RequestUtil.testKey()) {
                    final DonationCommand donationCommand = new DonationCommand(this);

                    this.bdsAutoEnable.getCommandManager().registerCommand(donationCommand);
                    restWebsite.register(new NotificationEndpoint(this, donationCommand));

                } else {
                    this.logger.error("&cTwój klucz&b API&c jest nie poprawny!");
                    this.bdsAutoEnable.getExtensionLoader().disableExtension(this);
                }
            }
        }
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