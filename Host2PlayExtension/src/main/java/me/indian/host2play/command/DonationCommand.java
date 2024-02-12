package me.indian.host2play.command;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import me.indian.bds.command.Command;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MathUtil;
import me.indian.discord.DiscordExtension;
import me.indian.discord.jda.manager.LinkingManager;
import me.indian.host2play.Host2PlayExtension;
import me.indian.host2play.component.payment.get.PaymentGet;
import me.indian.host2play.component.payment.post.PostReceivedData;
import me.indian.host2play.util.RequestUtil;
import org.jetbrains.annotations.Nullable;

public class DonationCommand extends Command {

    private final Host2PlayExtension extension;
    private final Map<String, Long> cooldown;
    private final Map<String, String> lastBuyers;

    public DonationCommand(final Host2PlayExtension extension) {
        super("donate", "");
        this.extension = extension;
        this.cooldown = new HashMap<>();
        this.lastBuyers = new HashMap<>();

    }


    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
//        if (this.commandSender == CommandSender.CONSOLE) {
//            return true;
//        }

        final long cooldownTime = MathUtil.minutesTo(1, TimeUnit.MILLISECONDS);
        if (args.length == 2) {
            final String emil = args[0];
            if (!emil.contains("@")) {
                this.sendMessage("&cTen emil nie jest poprawny!");
                return false;
            }

            final int moneyAmount;

            try {
                moneyAmount = Integer.parseInt(args[1]);
            } catch (final NumberFormatException exception) {
                this.sendMessage("&b" + args[1] + "&c nie jest liczbą");
                return false;
            }

            if (!this.cooldown.containsKey(this.playerName) || System.currentTimeMillis() - this.cooldown.get(this.playerName) > cooldownTime) {
                try {
                    final PostReceivedData receivedData = RequestUtil.createPaymentPost(emil, moneyAmount);

                    if (receivedData == null) {
                        this.sendMessage("&cNie udało się utworzyć płatności");
                        return true;
                    }

                    final String paymentID = receivedData.data().paymentId();
                    final PaymentGet paymentInfo = RequestUtil.getPayment(paymentID);

                    this.sendMessage("&aLink do transakcji:&b " + receivedData.data().paymentLink());
                    this.sendLinkToDiscord(receivedData.data().paymentLink(), this.playerName);
                    this.sendMessage("Twoja transakcja wygasa:&b " + DateUtil.longToLocalDate(paymentInfo.data().expires()));

                    this.lastBuyers.put(paymentID, this.playerName);
                } catch (final IOException ioException) {
                    this.sendMessage("&cNie udało się dokonać transakcji z powodu:&b " + ioException.getMessage());
                    return true;
                }

                this.cooldown.put(this.playerName, System.currentTimeMillis());

            } else {
                final long remainingTime = (cooldownTime - (this.cooldown.get(this.playerName) - System.currentTimeMillis()));
                this.sendMessage("&aMusisz odczekać:&b " + DateUtil.formatTime(remainingTime, List.of('m', 's')));
            }
        }
        return false;
    }

    private void sendLinkToDiscord(final String link, final String playerName) {
        final DiscordExtension discordExtension = this.extension.getDiscordExtension();
        if (discordExtension != null) {
            if (discordExtension.isBotEnabled()) {
                final LinkingManager linkingManager = discordExtension.getDiscordJDA().getLinkingManager();
                if (linkingManager != null) {
                    if (linkingManager.isLinked(playerName)) {
                        discordExtension.getDiscordJDA()
                                .sendPrivateMessage(linkingManager.getMember(playerName).getUser(),
                                        "Twój link do płatności to: " + link
                                );
                        this.sendMessage("&aWysłaliśmy link do transakcji do ciebie na discord , mamy nadzieje ze miałeś włączone prywatne wiadomości");
                    }
                }
            }
        }
    }

    @Nullable
    public String getBuyerName(final String paymentID) {
        return this.lastBuyers.get(paymentID);
    }
}
