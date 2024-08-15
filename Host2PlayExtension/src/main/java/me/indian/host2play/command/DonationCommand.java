package me.indian.host2play.command;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import me.indian.bds.command.Command;
import me.indian.bds.util.DateUtil;
import me.indian.discord.DiscordExtension;
import me.indian.discord.jda.manager.LinkingManager;
import me.indian.host2play.Host2PlayExtension;
import me.indian.host2play.component.payment.get.PaymentGet;
import me.indian.host2play.component.payment.post.PostReceivedData;
import me.indian.host2play.util.RequestUtil;
import org.jetbrains.annotations.Nullable;

public class DonationCommand extends Command {


    //TODO:Fix that

    private final Host2PlayExtension extension;
    private final Map<String, Long> cooldown;
    private final Map<String, String> lastBuyers;

    public DonationCommand(final Host2PlayExtension extension) {
        super("donate", "Pozwala na darowiznę servera");
        this.extension = extension;
        this.cooldown = new HashMap<>();
        this.lastBuyers = new HashMap<>();

        this.addOption("<emil> <ilość>", "Informacje do wykonania donate");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (this.player == null) {
            this.sendMessage("&cTe polecenie nie może zostać wykonane z poziomu konsoli");
            return true;
        }

        final String playerName = this.player.getPlayerName();
        final long cooldownTime = DateUtil.secondToMillis(90);

        if (args.length == 2) {
            final String emil = args[0];
            if (!emil.contains("@") && !emil.contains(".")) {
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

            if (!this.cooldown.containsKey(playerName) || System.currentTimeMillis() - this.cooldown.get(playerName) > cooldownTime) {
                try {
                    this.sendMessage("&aTworzenie transakcji");
                    this.cooldown.put(playerName, System.currentTimeMillis());

                    final PostReceivedData receivedData = RequestUtil.createPaymentPost(emil, moneyAmount);

                    if (receivedData == null) {
                        this.sendMessage("&cNie udało się utworzyć transakcji");
                        this.cooldown.remove(playerName);
                        return true;
                    }

                    this.sendMessage("&3----------");

                    final String paymentID = receivedData.data().paymentId();
                    final PaymentGet paymentInfo = RequestUtil.getPayment(paymentID);
                    final Instant instant = Instant.ofEpochSecond(paymentInfo.data().expires());
                    final String expireDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));


                    this.sendMessage("&aLink do transakcji:&b " + receivedData.data().paymentLink());
                    this.sendLinkToDiscord(receivedData.data().paymentLink(), playerName);
                    this.sendMessage("&aTwoja transakcja wygasa:&b " + expireDate);

                    this.sendMessage("&3----------");

                    this.lastBuyers.put(paymentID, playerName);
                    return true;
                } catch (final IOException ioException) {
                    this.cooldown.remove(playerName);
                    this.sendMessage("&cNie udało się dokonać transakcji z powodu:&b " + ioException.getMessage());
                    return true;
                }
            } else {
                final long playerCooldown = this.cooldown.getOrDefault(playerName, 0L);
                final long remainingTime = (playerCooldown + cooldownTime) - System.currentTimeMillis();

                this.sendMessage("&cAby dokonać kolejnej wpłaty musisz odczekać:&b " + DateUtil.formatTimeDynamic(remainingTime));
                return true;
            }
        }
        return false;
    }

    private void sendLinkToDiscord(final String link, final String playerName) {
        final DiscordExtension discordExtension = (DiscordExtension) this.extension.getBdsAutoEnable().getExtensionManager().getExtension("DiscordExtension");
        if (discordExtension != null) {
            if (discordExtension.isBotEnabled()) {
                final LinkingManager linkingManager = discordExtension.getDiscordJDA().getLinkingManager();
                if (linkingManager != null) {
                    if (linkingManager.isLinked(playerName)) {
                        discordExtension.getDiscordJDA()
                                .sendPrivateMessage(linkingManager.getMember(playerName).getUser(),
                                        "Twój link do płatności to: " + link);
                        this.sendMessage("&aWysłaliśmy link do transakcji do ciebie na discord , mamy nadzieje ze miałeś włączone prywatne wiadomości");
                    } else {
                        this.sendMessage("&cNie masz połączonych kont z Discord, nie mogliśmy wysłać ci linku w prywatnej wiadomości");
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