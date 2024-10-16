package pl.indianbartonka.effect.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pl.indianbartonka.bds.event.EventHandler;
import pl.indianbartonka.bds.event.Listener;
import pl.indianbartonka.bds.event.player.PlayerJoinEvent;
import pl.indianbartonka.bds.event.player.PlayerSpawnEvent;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.effect.EffectsExtension;
import pl.indianbartonka.effect.config.EffectsConfig;
import pl.indianbartonka.util.ThreadUtil;

public class PlayerListener implements Listener {

    private final ServerProcess serverProcess;
    private final ExecutorService effectsService;
    private final EffectsConfig config;

    public PlayerListener(final EffectsExtension extension) {
        this.serverProcess = extension.getBdsAutoEnable().getServerProcess();
        this.effectsService = Executors.newScheduledThreadPool(2, new ThreadUtil("Effects Service"));
        this.config = extension.getConfig();
    }


    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        this.effectsService.execute(() ->
                this.config.getOnJoin().forEach(command -> this.serverProcess.sendToConsole(command.replaceAll("<player>", event.getPlayer().getPlayerName()))));
    }

    @EventHandler
    private void onPlayerSpawn(final PlayerSpawnEvent event) {
        this.effectsService.execute(() ->
                this.config.getOnSpawn().forEach(command -> this.serverProcess.sendToConsole(command.replaceAll("<player>", event.getPlayer().getPlayerName()))));
    }
}