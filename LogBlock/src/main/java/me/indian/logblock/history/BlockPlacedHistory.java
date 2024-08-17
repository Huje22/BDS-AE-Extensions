package me.indian.logblock.history;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import me.indian.bds.event.Event;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.player.position.Position;
import me.indian.util.DateUtil;
import me.indian.logblock.Config;
import me.indian.logblock.LogBlockExtension;

public class BlockPlacedHistory extends History {

    private final Config config;
    private final Map<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> blockPlaceHistory;

    public BlockPlacedHistory(final LogBlockExtension logBlockExtension) {
        super(logBlockExtension, "BlockPlaced");
        this.config = logBlockExtension.getConfig();
        this.blockPlaceHistory = new LinkedHashMap<>();
    }

    @Override
    public void addToHistory(final Event eventToHistory) {
        if (eventToHistory instanceof final PlayerBlockPlaceEvent event) {
            final Position blockPosition = event.getBlockPosition();

            if (!this.blockPlaceHistory.containsKey(blockPosition)) {
                final Map<LocalDateTime, PlayerBlockPlaceEvent> playerBlockPlaceEvents = new HashMap<>();
                this.blockPlaceHistory.put(blockPosition, playerBlockPlaceEvents);
            }

            this.blockPlaceHistory.get(blockPosition).put(LocalDateTime.now(DateUtil.POLISH_ZONE), event);

            if (this.blockPlaceHistory.size() == this.config.getMaxBrokenBlockMapSize()) {
                this.clearHistory();
            }
        } else {
            this.logger.debug("&cNie można zapisać eventu&b " + eventToHistory.getEventName() + "&c ponieważ to nie&b " + PlayerBlockPlaceEvent.class.getName());
        }
    }

    @Override
    protected void clearHistory() {
        this.saveHistory();
        this.blockPlaceHistory.clear();
        this.startTime = DateUtil.getTimeHM().replace(":", "-");
    }

    public Map<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> getHistory() {
        return this.blockPlaceHistory;
    }

    @Override
    public void saveHistory() {
        this.saveToFile(this.blockPlaceHistory);
    }
}