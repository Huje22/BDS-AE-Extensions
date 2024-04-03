package me.indian.logblock.listener;

import me.indian.bds.event.Listener;
import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerInteractContainerEvent;
import me.indian.bds.event.player.PlayerInteractEntityWithContainerEvent;
import me.indian.logblock.LogBlockExtension;
import me.indian.logblock.history.HistoryManager;

public class PlayerListener extends Listener {

    private final HistoryManager historyManager;

    public PlayerListener(final LogBlockExtension logBlockExtension) {
        this.historyManager = logBlockExtension.getHistoryManager();
    }

    @Override
    public void onPlayerBreakBlock(final PlayerBlockBreakEvent event) {
        this.historyManager.getBrokenBlockHistory().addToHistory(event);
    }

    @Override
    public void onPlayerPlaceBlock(final PlayerBlockPlaceEvent event) {
        this.historyManager.getPlacedBlockHistory().addToHistory(event);
    }

    @Override
    public void onPlayerInteractContainerEvent(final PlayerInteractContainerEvent event) {
        this.historyManager.getOpenedContainerHistory().addToHistory(event);
    }

    @Override
    public void onPlayerInteractEntityWithContainerEvent(final PlayerInteractEntityWithContainerEvent event) {
        this.historyManager.getInteractedEntityWithContainerHistory().addToHistory(event);
    }
}
