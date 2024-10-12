package pl.indianbartonka.logblock.history;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.event.player.PlayerBlockBreakEvent;
import pl.indianbartonka.bds.player.position.Position;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.logblock.Config;
import pl.indianbartonka.logblock.LogBlockExtension;

public class BlockBrokenHistory extends History {

    private final Config config;
    private final Map<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> blockBreakHistory;

    public BlockBrokenHistory(final LogBlockExtension logBlockExtension) {
        super(logBlockExtension, "BlockBroken");
        this.config = logBlockExtension.getConfig();
        this.blockBreakHistory = new LinkedHashMap<>();
    }

    @Override
    public void addToHistory(final Event eventToHistory) {
        if (eventToHistory instanceof final PlayerBlockBreakEvent event) {
            final Position blockPosition = event.getBlockPosition();

            if (!this.blockBreakHistory.containsKey(blockPosition)) {
                final Map<LocalDateTime, PlayerBlockBreakEvent> blockBreakEvents = new HashMap<>();
                this.blockBreakHistory.put(blockPosition, blockBreakEvents);
            }

            this.blockBreakHistory.get(blockPosition).put(LocalDateTime.now(DateUtil.POLISH_ZONE), event);

            if (this.blockBreakHistory.size() == this.config.getMaxPlacedBlockMapSize()) {
                this.clearHistory();
            }
        } else {
            this.logger.debug("&cNie można zapisać eventu&b " + eventToHistory.getEventName() + "&c ponieważ to nie&b " + PlayerBlockBreakEvent.class.getName());
        }
    }

    @Override
    protected void clearHistory() {
        this.saveHistory();
        this.blockBreakHistory.clear();
        this.startTime = DateUtil.getTimeHM().replace(":", "-");
    }

    public Map<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> getHistory() {
        return this.blockBreakHistory;
    }

    @Override
    public void saveHistory() {
        this.saveToFile(this.blockBreakHistory);
    }
}
