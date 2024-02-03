package me.indian.discord.listener;

import me.indian.bds.event.Listener;
import me.indian.bds.event.server.ServerClosedEvent;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.ServerUpdatingEvent;
import me.indian.bds.event.server.TPSChangeEvent;
import me.indian.discord.DiscordExtension;
import me.indian.discord.jda.DiscordJDA;

public class ServerListener extends Listener {

    private final DiscordExtension discordExtension;
    private final DiscordJDA discordJDA;

    public ServerListener(final DiscordExtension discordExtension) {
        this.discordExtension = discordExtension;
        this.discordJDA = this.discordExtension.getDiscordJDA();
    }

    @Override
    public void onServerStart(final ServerStartEvent event) {
        this.discordJDA.sendEnabledMessage();
    }

    @Override
    public void onTpsChange(final TPSChangeEvent event) {
        final int tps = event.getTps();
        final int lastTPS = event.getLastTps();

        if (tps <= 8) this.discordJDA.sendMessage("Server posiada: **" + tps + "** TPS");
        if (lastTPS <= 8 && tps <= 8) {
            this.discordJDA.sendMessage("Zaraz nastąpi restartowanie servera z powodu niskiej ilości TPS"
                    + " (Teraz: **" + tps + "** Ostatnie: **" + lastTPS + "**)");
            this.discordExtension.getBdsAutoEnable()
                    .getWatchDog().getAutoRestartModule().restart(true, 10, "Niska ilość TPS");
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
}
