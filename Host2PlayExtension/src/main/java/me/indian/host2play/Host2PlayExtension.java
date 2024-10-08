package me.indian.host2play;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.util.logger.Logger;
import me.indian.bds.util.HTTPUtil;
import me.indian.host2play.command.DonationCommand;
import me.indian.host2play.config.Config;
import me.indian.host2play.listener.ExtensionEnableListener;
import me.indian.host2play.rest.NotificationEndpoint;
import me.indian.host2play.util.RequestUtil;
import me.indian.rest.RestWebsite;

public class Host2PlayExtension extends Extension {

    private BDSAutoEnable bdsAutoEnable;
    private Config config;
    private Logger logger;
    private String notificationEndpoint;
    private String fullNotificationEndpoint;

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
        final RestWebsite restWebsite = (RestWebsite) this.bdsAutoEnable.getExtensionManager().getExtension("RestWebsite");

        if (restWebsite != null) {
            if (restWebsite.isEnabled()) {
                this.notificationEndpoint = "api/notification/" + this.config.getEndpointUUID();
                this.fullNotificationEndpoint = "http://" + this.config.getIp()
                        + ":" + restWebsite.getConfig().getPort() + "/" + this.notificationEndpoint;

                RequestUtil.init(this);

                if (RequestUtil.testKey()) {
                    final DonationCommand donationCommand = new DonationCommand(this);
                    restWebsite.register(new NotificationEndpoint(this, donationCommand));

                    if (this.config.isDonateCommand()) {
                        this.bdsAutoEnable.getCommandManager().registerCommand(donationCommand, this);
                    }
                } else {
                    this.bdsAutoEnable.getEventManager().registerListener(new ExtensionEnableListener(this), this);
                    this.logger.error("&cTwój klucz&b API&c jest nie poprawny!");
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
}
