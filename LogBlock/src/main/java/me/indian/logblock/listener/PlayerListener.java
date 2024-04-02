package me.indian.logblock.listener;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.indian.bds.event.Listener;
import me.indian.bds.event.Position;
import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerInteractContainerEvent;
import me.indian.bds.event.player.PlayerInteractEntityWithContainerEvent;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.GsonUtil;
import me.indian.logblock.Config;
import me.indian.logblock.LogBlockExtension;
import me.indian.logblock.util.MarkDownUtil;

public class PlayerListener extends Listener {

    private final LogBlockExtension logBlockExtension;
    private final Config config;
    private final String startDate;
    private final Map<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> blockBreakHistory;
    private final Map<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> blockPlaceHistory;
    private final Map<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> openedContainerHistory;
    private final Map<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> interactedEntityWithContainerHistory;

    public PlayerListener(final LogBlockExtension logBlockExtension) {
        this.logBlockExtension = logBlockExtension;
        this.config = logBlockExtension.getConfig();
        this.startDate = String.valueOf(LocalDate.now(DateUtil.POLISH_ZONE));
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

        this.blockBreakHistory.get(blockPosition).put(LocalDateTime.now(DateUtil.POLISH_ZONE), event);

        if (this.blockBreakHistory.size() == this.config.getMaxBrokenBlockMapSize()) {
            this.saveBlockBreakHistory();
            this.blockBreakHistory.clear();
        }
    }

    @Override
    public void onPlayerPlaceBlock(final PlayerBlockPlaceEvent event) {
        final Position blockPosition = event.getBlockPosition();

        if (!this.blockPlaceHistory.containsKey(blockPosition)) {
            final Map<LocalDateTime, PlayerBlockPlaceEvent> playerBlockPlaceEvents = new HashMap<>();
            this.blockPlaceHistory.put(blockPosition, playerBlockPlaceEvents);
        }

        this.blockPlaceHistory.get(blockPosition).put(LocalDateTime.now(DateUtil.POLISH_ZONE), event);

        if (this.blockPlaceHistory.size() == this.config.getMaxPlacedBlockMapSize()) {
            this.saveBlockPlaceHistory();
            this.blockPlaceHistory.clear();
        }
    }

    @Override
    public void onPlayerInteractContainerEvent(final PlayerInteractContainerEvent event) {
        final Position blockPosition = event.getBlockPosition();

        if (!this.openedContainerHistory.containsKey(blockPosition)) {
            final Map<LocalDateTime, PlayerInteractContainerEvent> playerInteractContainerEvents = new HashMap<>();
            this.openedContainerHistory.put(blockPosition, playerInteractContainerEvents);
        }

        this.openedContainerHistory.get(blockPosition).put(LocalDateTime.now(DateUtil.POLISH_ZONE), event);

        if (this.openedContainerHistory.size() == this.config.getMaxOpenedContainerMapSize()) {
            this.saveOpenedContainerHistory();
            this.openedContainerHistory.clear();
        }
    }

    @Override
    public void onPlayerInteractEntityWithContainerEvent(final PlayerInteractEntityWithContainerEvent event) {
        final Position blockPosition = event.getEntityPosition();

        if (!this.interactedEntityWithContainerHistory.containsKey(blockPosition)) {
            final Map<LocalDateTime, PlayerInteractEntityWithContainerEvent> playerInteractEntityWithContainerEvents = new HashMap<>();
            this.interactedEntityWithContainerHistory.put(blockPosition, playerInteractEntityWithContainerEvents);
        }

        this.interactedEntityWithContainerHistory.get(blockPosition).put(LocalDateTime.now(DateUtil.POLISH_ZONE), event);

        if (this.interactedEntityWithContainerHistory.size() == this.config.getMaxInteractedEntityWithContainer()) {
            this.saveInteractedEntityMap();
            this.interactedEntityWithContainerHistory.clear();
        }
    }

    private void saveBlockBreakHistory() {
        if (!this.saveToFile("BlockBreakHistory.md", this.blockBreakHistory)) {
            this.saveToFile("BlockBreakHistory.md", this.blockBreakHistory);
        }
    }

    private void saveBlockPlaceHistory() {
        if (!this.saveToFile("BlockPlaceHistory.md", this.blockPlaceHistory)) {
            this.saveToFile("BlockPlaceHistory.md", this.blockPlaceHistory);
        }
    }

    private void saveOpenedContainerHistory() {
        if (!this.saveToFile("OpenedContainerHistory.md", this.openedContainerHistory)) {
            this.saveToFile("OpenedContainerHistory.md", this.openedContainerHistory);
        }
    }

    private void saveInteractedEntityMap() {
        if (!this.saveToFile("InteractedEntityContainerHistory.md", this.interactedEntityWithContainerHistory)) {
            this.saveToFile("InteractedEntityContainerHistory.md", this.interactedEntityWithContainerHistory);
        }
    }

    public void saveAllMaps() {
        this.saveBlockBreakHistory();
        this.saveBlockPlaceHistory();
        this.saveOpenedContainerHistory();
        this.saveInteractedEntityMap();
    }

    private <T> boolean saveToFile(final String fileName, final Map<Position, Map<LocalDateTime, T>> map) {
        if (map.isEmpty()) return true;
        try {
            String date = String.valueOf(LocalDate.now(DateUtil.POLISH_ZONE));
            final String dateNow = String.valueOf(LocalDate.now(DateUtil.POLISH_ZONE));

            if(!dateNow.equals(this.startDate)){
                date = this.startDate + "-" + dateNow;
            }

            final String path = this.logBlockExtension.getDataFolder() + File.separator + date + File.separator;
            final File file = new File(path + DateUtil.getTimeHMS().replace(":", "-") + " " + fileName);
            Files.createDirectories(Path.of(path));
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return false;
                }
            }

            final Gson gson = GsonUtil.getGson();

            try (final FileWriter writer = new FileWriter(file)) {
                writer.write("# Dostępne jest aż " + map.size() + " pozycji \n\n");

                for (final Map.Entry<Position, Map<LocalDateTime, T>> entry : map.entrySet()) {
                    final Map<LocalDateTime, T> l = entry.getValue();

                    final List<Map.Entry<LocalDateTime, T>> sortedEntries = new ArrayList<>(l.entrySet());
                    sortedEntries.sort(Map.Entry.comparingByKey());

                    for (final Map.Entry<LocalDateTime, T> sortedEntry : sortedEntries) {
                        final LocalDateTime dateTime = sortedEntry.getKey();
                        final T value = sortedEntry.getValue();

                        String event = "";

                        if (value instanceof PlayerBlockBreakEvent) {
                            event = gson.toJson(value, PlayerBlockBreakEvent.class);
                        } else if (value instanceof PlayerBlockPlaceEvent) {
                            event = gson.toJson(value, PlayerBlockPlaceEvent.class);
                        } else if (value instanceof PlayerInteractContainerEvent) {
                            event = gson.toJson(value, PlayerInteractContainerEvent.class);
                        } else if (value instanceof PlayerInteractEntityWithContainerEvent) {
                            event = gson.toJson(value, PlayerInteractEntityWithContainerEvent.class);
                        }

                        writer.write(MarkDownUtil.formatInfo(this.getTime(dateTime), event));
                    }
                }
            }

            this.logBlockExtension.getLogger().info("&aZapisano pomyślnie plik&e " + fileName);
            return true;
        } catch (final Exception exception) {
            this.logBlockExtension.getLogger().critical("&cNie udało się zapisać pliku&e " + fileName, exception);
            return false;
        }
    }

    private String getTime(final LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"));
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
