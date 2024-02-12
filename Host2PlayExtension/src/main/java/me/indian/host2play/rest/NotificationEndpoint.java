package me.indian.host2play.rest;

import io.javalin.Javalin;
import me.indian.bds.logger.LogState;
import me.indian.bds.util.GsonUtil;
import me.indian.discord.DiscordExtension;
import me.indian.host2play.Host2PlayExtension;
import me.indian.host2play.command.DonationCommand;
import me.indian.host2play.component.Notification;
import me.indian.host2play.component.payment.get.PaymentGet;
import me.indian.host2play.config.Config;
import me.indian.host2play.util.RequestUtil;
import me.indian.rest.HttpHandler;

public class NotificationEndpoint extends HttpHandler {

    private final Host2PlayExtension extension;
    private final DonationCommand donationCommand;
    private final Config config;


    public NotificationEndpoint(final Host2PlayExtension extension, final DonationCommand donationCommand) {
        this.extension = extension;
        this.donationCommand = donationCommand;
        this.config = extension.getConfig();
    }


    @Override
    public void handle(final Javalin app) {
        app.post(this.extension.getNotificationEndpoint(), ctx -> {
            final String requestBody = ctx.body();
            final Notification notification = GsonUtil.getGson().fromJson(requestBody, Notification.class);
            final PaymentGet info = RequestUtil.getPayment(notification.data().paymentId());

            final String playerName = this.donationCommand.getBuyerName(notification.data().paymentId());

            if (playerName != null) {
                this.extension.getBdsAutoEnable().getServerProcess().tellrawToAllAndLogger("",
                        "&aGracz &l" + playerName + "&r&a zasponsorował server, pięniędzmi o wysokości:&b " + info.data().amount() + "&e PLN",
                        LogState.INFO);
                this.sendDiscordAlert("Gracz **" + playerName + "** zasponsorował server, pięniędzmi o wysokości: **" + info.data().amount() + "** PLN");
            } else {
                this.extension.getBdsAutoEnable().getServerProcess().tellrawToAllAndLogger("",
                        "&aUzytkownik z emilem &l" + info.data().customerEmail() + "&r&a zasponsorował server, pięniędzmi o wysokości:&b " + info.data().amount() + "&e PLN",
                        LogState.INFO);
                this.sendDiscordAlert("Uzytkownik z emilem **" + info.data().customerEmail() + "** zasponsorował server, pięniędzmi o wysokości: **" + info.data().amount() + "** PLN");
            }
        });
    }

    private void sendDiscordAlert(final String alert) {

        final DiscordExtension discordExtension = this.extension.getDiscordExtension();
        if (discordExtension != null) {
            if (discordExtension.isBotEnabled()) {
                discordExtension.getDiscordJDA().sendMessage(alert);
            } else {
                if (discordExtension.isWebhookEnabled()) {
                    discordExtension.getWebHook().sendMessage(alert);
                }
            }
        }
    }
}
