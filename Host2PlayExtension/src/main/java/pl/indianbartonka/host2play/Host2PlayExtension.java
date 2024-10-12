package pl.indianbartonka.host2play;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.extension.Extension;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.bds.util.HTTPUtil;
import pl.indianbartonka.host2play.command.DonationCommand;
import pl.indianbartonka.host2play.config.Config;
import pl.indianbartonka.host2play.listener.ExtensionEnableListener;
import pl.indianbartonka.host2play.rest.NotificationEndpoint;
import pl.indianbartonka.host2play.util.RequestUtil;
import pl.indianbartonka.rest.RestWebsite;

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
