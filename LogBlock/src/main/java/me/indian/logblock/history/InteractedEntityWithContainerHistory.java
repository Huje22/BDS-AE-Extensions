package me.indian.logblock.history;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import me.indian.bds.event.Event;
import me.indian.bds.event.Position;
import me.indian.bds.event.player.PlayerInteractEntityWithContainerEvent;
import me.indian.bds.util.DateUtil;
import me.indian.logblock.Config;
import me.indian.logblock.LogBlockExtension;

public class InteractedEntityWithContainerHistory extends History {

    private final Config config;
    private final Map<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> interactedEntityWithContainerHistory;

    public InteractedEntityWithContainerHistory(final LogBlockExtension logBlockExtension) {
        super(logBlockExtension, "InteractedEntityWithContainer");
        this.config = logBlockExtension.getConfig();
        this.interactedEntityWithContainerHistory = new LinkedHashMap<>();
    }

    @Override
    public void addToHistory(final Event eventToHistory) {
        if (eventToHistory instanceof final PlayerInteractEntityWithContainerEvent event) {
            final Position blockPosition = event.getEntityPosition();

            if (!this.interactedEntityWithContainerHistory.containsKey(blockPosition)) {
                final Map<LocalDateTime, PlayerInteractEntityWithContainerEvent> playerInteractEntityWithContainerEvents = new HashMap<>();
                this.interactedEntityWithContainerHistory.put(blockPosition, playerInteractEntityWithContainerEvents);
            }

            this.interactedEntityWithContainerHistory.get(blockPosition).put(LocalDateTime.now(DateUtil.POLISH_ZONE), event);

            if (this.interactedEntityWithContainerHistory.size() == this.config.getMaxInteractedEntityWithContainer()) {
                this.clearHistory();
            }
        } else {
            this.logger.debug("&cNie można zapisać eventu&b " + eventToHistory.getEventName() + "&c ponieważ to nie&b " + PlayerInteractEntityWithContainerEvent.class.getName());
        }
    }

    @Override
    protected void clearHistory() {
        this.saveHistory();
        this.interactedEntityWithContainerHistory.clear();
        this.startTime = DateUtil.getTimeHM().replace(":", "-");
    }

    public Map<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> getHistory() {
        return this.interactedEntityWithContainerHistory;
    }

    @Override
    public void saveHistory() {
        this.saveToFile(this.interactedEntityWithContainerHistory);
    }
}
