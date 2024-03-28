package me.indian.logblock.listener;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import me.indian.bds.event.Listener;
import me.indian.bds.event.Position;
import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerInteractContainerEvent;
import me.indian.bds.event.player.PlayerInteractEntityWithContainerEvent;
import me.indian.bds.util.GsonUtil;
import me.indian.logblock.LogBlockExtension;

public class PlayerListener extends Listener {

    private final LogBlockExtension logBlockExtension;
    private final int maxSize;
    private final Map<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> blockBreakHistory;
    private final Map<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> blockPlaceHistory;
    private final Map<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> openedContainerHistory;
    private final Map<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> interactedEntityWithContainerHistory;

    public PlayerListener(final LogBlockExtension logBlockExtension) {
        this.logBlockExtension = logBlockExtension;
        this.maxSize = logBlockExtension.getConfig().getMaxMapSize();
        this.blockBreakHistory = new LinkedHashMap<>();
        this.blockPlaceHistory = new LinkedHashMap<>();
        this.openedContainerHistory = new LinkedHashMap<>();
        this.interactedEntityWithContainerHistory = new LinkedHashMap<>();
    }

    @Override
    public void onPlayerBreakBlock(final PlayerBlockBreakEvent event) {
        final Position blockPosition = event.getBlockPosition();

        if (!this.blockBreakHistory.containsKey(blockPosition)) {
            final Map<LocalDateTime, PlayerBlockBreakEvent> blockBreakEvents = new HashMap<>();
            this.blockBreakHistory.put(blockPosition, blockBreakEvents);
        }

        this.blockBreakHistory.get(blockPosition).put(LocalDateTime.now(), event);

        if (this.blockBreakHistory.size() == this.maxSize) {
            this.saveBlockBreakHistory();
        }
    }

    @Override
    public void onPlayerPlaceBlock(final PlayerBlockPlaceEvent event) {
        final Position blockPosition = event.getBlockPosition();

        if (!this.blockPlaceHistory.containsKey(blockPosition)) {
            final Map<LocalDateTime, PlayerBlockPlaceEvent> playerBlockPlaceEvents = new HashMap<>();
            this.blockPlaceHistory.put(blockPosition, playerBlockPlaceEvents);
        }

        this.blockPlaceHistory.get(blockPosition).put(LocalDateTime.now(), event);

        if (this.blockPlaceHistory.size() == this.maxSize) {
            this.saveBlockPlaceHistory();
        }
    }

    @Override
    public void onPlayerInteractContainerEvent(final PlayerInteractContainerEvent event) {
        final Position blockPosition = event.getBlockPosition();

        if (!this.openedContainerHistory.containsKey(blockPosition)) {
            final Map<LocalDateTime, PlayerInteractContainerEvent> playerInteractContainerEvents = new HashMap<>();
            this.openedContainerHistory.put(blockPosition, playerInteractContainerEvents);
        }

        this.openedContainerHistory.get(blockPosition).put(LocalDateTime.now(), event);

        if (this.openedContainerHistory.size() == this.maxSize) {
            this.saveOpenedContainerHistory();
        }
    }

    @Override
    public void onPlayerInteractEntityWithContainerEvent(final PlayerInteractEntityWithContainerEvent event) {
        final Position blockPosition = event.getBlockPosition();

        if (!this.interactedEntityWithContainerHistory.containsKey(blockPosition)) {
            final Map<LocalDateTime, PlayerInteractEntityWithContainerEvent> playerInteractEntityWithContainerEvents = new HashMap<>();
            this.interactedEntityWithContainerHistory.put(blockPosition, playerInteractEntityWithContainerEvents);
        }

        this.interactedEntityWithContainerHistory.get(blockPosition).put(LocalDateTime.now(), event);

        if (this.interactedEntityWithContainerHistory.size() == this.maxSize) {
            this.saveInteractedEntityMap();
        }
    }

    private void saveBlockBreakHistory() {
        if (!this.saveToFile("BlockBreakHistory.txt", this.blockBreakHistory)) {
            this.saveToFile("BlockBreakHistory.txt", this.blockBreakHistory);
        }
    }

    private void saveBlockPlaceHistory() {
        if (!this.saveToFile("BlockPlaceHistory.txt", this.blockPlaceHistory)) {
            this.saveToFile("BlockPlaceHistory.txt", this.blockPlaceHistory);
        }
    }

    private void saveOpenedContainerHistory() {
        if (!this.saveToFile("OpenedContainerHistory.txt", this.openedContainerHistory)) {
            this.saveToFile("OpenedContainerHistory.txt", this.openedContainerHistory);
        }
    }

    private void saveInteractedEntityMap() {
        if (!this.saveToFile("InteractedEntityContainerHistory.txt", this.interactedEntityWithContainerHistory)) {
            this.saveToFile("InteractedEntityContainerHistory.txt", this.interactedEntityWithContainerHistory);
        }
    }

    public void saveAllMaps() {
        this.saveBlockBreakHistory();
        this.saveBlockPlaceHistory();
        this.saveOpenedContainerHistory();
        this.saveInteractedEntityMap();
    }

    private boolean saveToFile(final String fileName, final Map map) {
        try {
            final File file = new File(this.logBlockExtension.getDataFolder() + File.separator + fileName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return false;
                }
            }

            try (final FileWriter writer = new FileWriter(file)) {
                writer.write(GsonUtil.getGson().toJson(map));
            }

            this.logBlockExtension.getLogger().info("&aZapisano pomyślnie plik&e " + fileName);
            return true;
        } catch (final Exception exception) {
            this.logBlockExtension.getLogger().critical("&cNie udało się zapisać pliku&e " + fileName, exception);
            return false;
        }
    }

    public Map<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> getBlockBreakHistory() {
        return this.blockBreakHistory;
    }

    public Map<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> getBlockPlaceHistory() {
        return this.blockPlaceHistory;
    }

    public Map<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> getOpenedContainerHistory() {
        return this.openedContainerHistory;
    }

    public Map<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> getInteractedEntityWithContainerHistory() {
        return this.interactedEntityWithContainerHistory;
    }
}