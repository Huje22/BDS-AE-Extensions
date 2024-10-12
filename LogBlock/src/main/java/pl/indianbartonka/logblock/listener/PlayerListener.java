package pl.indianbartonka.logblock.listener;

import pl.indianbartonka.bds.event.EventHandler;
import pl.indianbartonka.bds.event.Listener;
import pl.indianbartonka.bds.event.player.PlayerBlockBreakEvent;
import pl.indianbartonka.bds.event.player.PlayerBlockPlaceEvent;
import pl.indianbartonka.bds.event.player.PlayerInteractContainerEvent;
import pl.indianbartonka.bds.event.player.PlayerInteractEntityWithContainerEvent;
import pl.indianbartonka.logblock.LogBlockExtension;
import pl.indianbartonka.logblock.history.HistoryManager;

public class PlayerListener implements Listener {

    private final HistoryManager historyManager;

    public PlayerListener(final LogBlockExtension logBlockExtension) {
        this.historyManager = logBlockExtension.getHistoryManager();
    }

    @EventHandler
    private void onPlayerBreakBlock(final PlayerBlockBreakEvent event) {
        this.historyManager.getBrokenBlockHistory().addToHistory(event);
    }

    @EventHandler
    private void onPlayerPlaceBlock(final PlayerBlockPlaceEvent event) {
        this.historyManager.getPlacedBlockHistory().addToHistory(event);
    }

    @EventHandler
    private void onPlayerInteractContainerEvent(final PlayerInteractContainerEvent event) {
        this.historyManager.getOpenedContainerHistory().addToHistory(event);
    }

    @EventHandler
    private void onPlayerInteractEntityWithContainerEvent(final PlayerInteractEntityWithContainerEvent event) {
        this.historyManager.getInteractedEntityWithContainerHistory().addToHistory(event);
    }
}
