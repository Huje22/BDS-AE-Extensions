package pl.indianbartonka.host2play.rest;

import io.javalin.Javalin;
import pl.indianbartonka.util.logger.LogState;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.bds.util.GsonUtil;
import pl.indianbartonka.bds.util.ServerUtil;
import pl.indianbartonka.discord.DiscordExtension;
import pl.indianbartonka.discord.jda.manager.LinkingManager;
import pl.indianbartonka.host2play.Host2PlayExtension;
import pl.indianbartonka.host2play.command.DonationCommand;
import pl.indianbartonka.host2play.component.Notification;
import pl.indianbartonka.host2play.component.payment.get.PaymentGet;
import pl.indianbartonka.host2play.component.payment.get.PaymentSubData;
import pl.indianbartonka.host2play.config.Config;
import pl.indianbartonka.host2play.util.RequestUtil;
import pl.indianbartonka.rest.HttpHandler;

public class NotificationEndpoint extends HttpHandler {

    private final Host2PlayExtension extension;
    private final Logger logger;
    private final DonationCommand donationCommand;
    private final Config config;

    public NotificationEndpoint(final Host2PlayExtension extension, final DonationCommand donationCommand) {
        this.extension = extension;
        this.logger = this.extension.getLogger();
        this.donationCommand = donationCommand;
        this.config = extension.getConfig();
    }

    @Override
    public void handle(final Javalin app) {
        app.post(this.extension.getNotificationEndpoint(), ctx -> {
            try {
                final String requestBody = ctx.body();
                this.logger.debug(requestBody);
                final Notification notification = GsonUtil.getGson().fromJson(requestBody, Notification.class);

                if (notification == null) {
                    this.logger.error("&cNie udało się przetworzyć płatności:&b " + requestBody.replaceAll("\n", "") + "&c od IP:&b " + ctx.ip());
                    return;
                }

                final PaymentGet paymentInfo = RequestUtil.getPayment(notification.paymentId());

                if (paymentInfo == null) {
                    this.logger.error("&cNie udało się przetworzyć płatności:&b " + requestBody.replaceAll("\n", "") + "&c od IP:&b " + ctx.ip());
                    return;
                }

                final PaymentSubData paymentData = paymentInfo.data();
                if (paymentData == null) {
                    this.logger.error("&cNie udało się przetworzyć płatności:&b " + requestBody.replaceAll("\n", "") + "&c od IP:&b " + ctx.ip());
                    return;
                }

                final String playerName = this.donationCommand.getBuyerName(notification.paymentId());

                if (playerName != null) {
                    ServerUtil.tellrawToAllAndLogger("",
                            "&aGracz &l" + playerName + "&r&a zasponsorował server, pięniędzmi o wysokości:&b " + paymentData.amount() + "&e PLN", LogState.INFO);
                    this.sendDiscordAlert(playerName,
                            "Gracz **" + playerName + "** zasponsorował server, pięniędzmi o wysokości: **" + paymentData.amount() + "** PLN", paymentData);
                } else {
                    ServerUtil.tellrawToAllAndLogger("",
                            "&aUżytkownik z emilem &b" + paymentData.customerEmail() + "&r&a zasponsorował server, pięniędzmi o wysokości:&b " + paymentData.amount() + "&e PLN",
                            LogState.INFO);
                    this.sendDiscordAlert("",
                            "Użytkownik z emilem **" + paymentData.customerEmail() + "** zasponsorował server, pięniędzmi o wysokości: **" + paymentData.amount() + "** PLN",
                            paymentData
                    );
                }
            } catch (final Exception exception) {
                this.logger.debug("Nie udało się przetworzyć informacji o płatności, wysłało je ip:&b " + ctx.ip(), exception);
            }
        });
    }

    private void sendDiscordAlert(final String playerName, final String alert, final PaymentSubData data) {
        final DiscordExtension discordExtension = (DiscordExtension) this.extension.getBdsAutoEnable().getExtensionManager().getExtension("DiscordExtension");
        if (discordExtension != null) {
            if (discordExtension.isBotEnabled()) {
                discordExtension.getDiscordJDA().sendMessage(alert);

                final LinkingManager linkingManager = (LinkingManager) discordExtension.getDiscordJDA().getLinkingManager();
                if (linkingManager != null) {
                    if (linkingManager.isLinked(playerName)) {
                        discordExtension.getDiscordJDA()
                                .sendPrivateMessage(linkingManager.getMember(playerName).getUser(),
                                        "Twoja płatność o wysokości **" + data.amount() + "**PLN została sfinalizowana");
                    }
                }
            } else {
                if (discordExtension.isWebhookEnabled()) {
                    discordExtension.getWebHook().sendMessage(alert);
                }
            }
        }
    }
}
