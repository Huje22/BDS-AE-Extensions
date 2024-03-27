package me.indian.discord.listener;

import java.util.LinkedList;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.Listener;
import me.indian.bds.event.server.ServerAlertEvent;
import me.indian.bds.event.server.ServerClosedEvent;
import me.indian.bds.event.server.ServerRestartEvent;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.ServerUncaughtExceptionEvent;
import me.indian.bds.event.server.ServerUpdatingEvent;
import me.indian.bds.event.server.TPSChangeEvent;
import me.indian.bds.logger.LogState;
import me.indian.bds.util.MessageUtil;
import me.indian.discord.DiscordExtension;
import me.indian.discord.embed.component.Field;
import me.indian.discord.embed.component.Footer;
import me.indian.discord.jda.DiscordJDA;

public class ServerListener extends Listener {

    private final DiscordJDA discordJDA;
    private final BDSAutoEnable bdsAutoEnable;
    private double tps, lastTPS;

    public ServerListener(final DiscordExtension discordExtension) {
        this.discordJDA = discordExtension.getDiscordJDA();
        this.bdsAutoEnable = discordExtension.getBdsAutoEnable();
    }

    @Override
    public void onServerStart(final ServerStartEvent event) {
        this.discordJDA.sendEnabledMessage();
    }

    @Override
    public void onTpsChange(final TPSChangeEvent event) {
        this.tps = event.getTps();
        this.lastTPS = event.getLastTps();

        if (this.tps <= 8) this.discordJDA.sendMessage("Server posiada: **" + this.tps + "** TPS");
    }

    @Override
    public void onServerRestart(final ServerRestartEvent event) {
        final String reason = event.getReason();

        if (reason == null) return;
        if (reason.contains("Niska ilość tps")) {
            this.discordJDA.sendMessage("Zaraz nastąpi restartowanie servera z powodu niskiej ilości TPS"
                    + " (Teraz: **" + this.tps + "** Ostatnie: **" + this.lastTPS + "**)");
        } else {
            this.discordJDA.sendMessage("Zaraz nastąpi restartowanie servera z powodu: **" + reason + "**");
        }
    }

    @Override
    public void onServerClose(final ServerClosedEvent event) {
        this.discordJDA.sendDisabledMessage();
    }

    @Override
    public void onServerAlert(final ServerAlertEvent event) {
        final List<Field> fieldList = new LinkedList<>();
        final String additionalInfo = event.getAdditionalInfo();
        final LogState state = event.getAlertState();

        if (event.getThrowable() != null) {
            fieldList.add(new Field("Wyjątek", "```" + MessageUtil.getStackTraceAsString(event.getThrowable()) + "```", false));
        }

        if (state == LogState.INFO || state == LogState.NONE) {
            //TODO: Takie elerty dawaj bez embeda
            this.discordJDA.sendEmbedMessage("Alert " + state, event.getMessage(),
                    fieldList,
                    new Footer((additionalInfo == null ? "" : additionalInfo)));
        } else {
            this.discordJDA.log("Alert " + state, event.getMessage(),
                    fieldList,
                    new Footer((additionalInfo == null ? "" : additionalInfo)));
        }
    }

    @Override
    public void onServerUpdating(final ServerUpdatingEvent event) {
        this.discordJDA.sendServerUpdateMessage(event.getVersion());
    }

    @Override
    public void onServerUncaughtException(final ServerUncaughtExceptionEvent event) {
        this.discordJDA.log("Niezłapany wyjątek", "**Wykryto niezłapany wyjątek**",
                List.of(new Field("Wystąpił w wątku", event.getThread().getName(), true),
                        new Field("Wyjątek", "```" + MessageUtil.getStackTraceAsString(event.getThrowable()) + "```", false)),
                new Footer(""));
    }
}
