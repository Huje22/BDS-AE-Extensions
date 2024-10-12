package pl.indianbartonka.logblock.history;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import pl.indianbartonka.bds.event.Event;
import pl.indianbartonka.bds.event.player.PlayerInteractContainerEvent;
import pl.indianbartonka.bds.player.position.Position;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.logblock.Config;
import pl.indianbartonka.logblock.LogBlockExtension;

public class OpenedContainerHistory extends History {

    private final Config config;
    private final Map<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> openedContainerHistory;

    public OpenedContainerHistory(final LogBlockExtension logBlockExtension) {
        super(logBlockExtension, "OpenedContainer");
        this.config = logBlockExtension.getConfig();
        this.openedContainerHistory = new LinkedHashMap<>();
    }

    @Override
    public void addToHistory(final Event eventToHistory) {
        if (eventToHistory instanceof final PlayerInteractContainerEvent event) {
            final Position blockPosition = event.getBlockPosition();

            if (!this.openedContainerHistory.containsKey(blockPosition)) {
                final Map<LocalDateTime, PlayerInteractContainerEvent> playerInteractContainerEvents = new HashMap<>();
                this.openedContainerHistory.put(blockPosition, playerInteractContainerEvents);
            }

            this.openedContainerHistory.get(blockPosition).put(LocalDateTime.now(DateUtil.POLISH_ZONE), event);

            if (this.openedContainerHistory.size() == this.config.getMaxOpenedContainerMapSize()) {
                this.clearHistory();
            }
        } else {
            this.logger.debug("&cNie można zapisać eventu&b " + eventToHistory.getEventName() + "&c ponieważ to nie&b " + PlayerInteractContainerEvent.class.getName());
        }
    }

    @Override
    protected void clearHistory() {
        this.saveHistory();
        this.openedContainerHistory.clear();
        this.startTime = DateUtil.getTimeHM().replace(":", "-");
    }

    public Map<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> getHistory() {
        return this.openedContainerHistory;
    }

    @Override
    public void saveHistory() {
        this.saveToFile(this.openedContainerHistory);
    }
}
