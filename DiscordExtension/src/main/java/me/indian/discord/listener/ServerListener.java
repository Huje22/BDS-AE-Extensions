package me.indian.discord.listener;

import java.util.List;
import me.indian.bds.event.Listener;
import me.indian.bds.event.server.ServerClosedEvent;
import me.indian.bds.event.server.ServerRestartEvent;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.ServerUncaughtExceptionEvent;
import me.indian.bds.event.server.ServerUpdatingEvent;
import me.indian.bds.event.server.TPSChangeEvent;
import me.indian.bds.util.MessageUtil;
import me.indian.discord.DiscordExtension;
import me.indian.discord.embed.component.Field;
import me.indian.discord.embed.component.Footer;
import me.indian.discord.jda.DiscordJDA;

public class ServerListener extends Listener {

    private final DiscordJDA discordJDA;
    private double tps, lastTPS;

    public ServerListener(final DiscordExtension discordExtension) {
        this.discordJDA = discordExtension.getDiscordJDA();
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
        if (this.lastTPS <= 8 && this.tps <= 8) {
            this.discordJDA.sendMessage("Zaraz nastąpi restartowanie servera z powodu niskiej ilości TPS"
                    + " (Teraz: **" + this.tps + "** Ostatnie: **" + this.lastTPS + "**)");
        }
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
    public void onServerUpdating(final ServerUpdatingEvent event) {
        this.discordJDA.sendServerUpdateMessage(event.getVersion());
    }

    @Override
    public void onServerUncaughtException(final ServerUncaughtExceptionEvent event) {
        this.discordJDA.log("Niezłapany wyjątek", "",
                List.of(new Field("Wystąpił w wątku", event.getThread().getName(), true),
                        new Field("Wyjątek", MessageUtil.getStackTraceAsString(event.getThrowable()), true)
                ),
                new Footer(""));
    }
}