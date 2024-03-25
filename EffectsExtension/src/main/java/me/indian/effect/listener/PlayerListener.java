package me.indian.effect.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.indian.bds.event.Listener;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerSpawnEvent;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.ThreadUtil;
import me.indian.effect.EffectsExtension;
import me.indian.effect.config.EffectsConfig;

public class PlayerListener extends Listener {

    private final ServerProcess serverProcess;
    private final ExecutorService effectsService;
    private final EffectsConfig config;

    public PlayerListener(final EffectsExtension extension) {
        this.serverProcess = extension.getBdsAutoEnable().getServerProcess();
        this.effectsService = Executors.newScheduledThreadPool(2, new ThreadUtil("Effects Service"));
        this.config = extension.getConfig();
    }


    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.effectsService.execute(() ->
                this.config.getOnJoin().forEach(command -> this.serverProcess.sendToConsole(command.replaceAll("<player>", event.getPlayerName()))));
    }

    @Override
    public void onPlayerSpawn(final PlayerSpawnEvent event) {
        this.effectsService.execute(() ->
                this.config.getOnSpawn().forEach(command -> this.serverProcess.sendToConsole(command.replaceAll("<player>", event.getPlayerName()))));

    }
}