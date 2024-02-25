package me.indian.host2play.rest;

import io.javalin.Javalin;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.discord.DiscordExtension;
import me.indian.discord.jda.manager.LinkingManager;
import me.indian.host2play.Host2PlayExtension;
import me.indian.host2play.command.DonationCommand;
import me.indian.host2play.component.Notification;
import me.indian.host2play.component.payment.get.PaymentGet;
import me.indian.host2play.component.payment.get.PaymentSubData;
import me.indian.host2play.config.Config;
import me.indian.host2play.util.RequestUtil;
import me.indian.rest.HttpHandler;

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
                    this.extension.getBdsAutoEnable().getServerProcess().tellrawToAllAndLogger("",
                            "&aGracz &l" + playerName + "&r&a zasponsorował server, pięniędzmi o wysokości:&b " + paymentData.amount() + "&e PLN", LogState.INFO);
                    this.sendDiscordAlert(playerName,
                            "Gracz **" + playerName + "** zasponsorował server, pięniędzmi o wysokości: **" + paymentData.amount() + "** PLN", paymentData);
                } else {
                    this.extension.getBdsAutoEnable().getServerProcess().tellrawToAllAndLogger("",
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

                final LinkingManager linkingManager = discordExtension.getDiscordJDA().getLinkingManager();
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
