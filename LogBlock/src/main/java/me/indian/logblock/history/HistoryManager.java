package me.indian.logblock.history;

import me.indian.logblock.LogBlockExtension;

public class HistoryManager {

    public final LogBlockExtension extension;
    private final BlockBrokenHistory blockBrokenHistory;
    private final BlockPlacedHistory blockPlacedHistory;
    private final OpenedContainerHistory openedContainerHistory;
    private final InteractedEntityWithContainerHistory interactedEntityWithContainerHistory;

    public HistoryManager(final LogBlockExtension extension) {
        this.extension = extension;
        this.blockBrokenHistory = new BlockBrokenHistory(extension);
        this.blockPlacedHistory = new BlockPlacedHistory(extension);
        this.openedContainerHistory = new OpenedContainerHistory(extension);
        this.interactedEntityWithContainerHistory = new InteractedEntityWithContainerHistory(extension);
    }

    public BlockBrokenHistory getBrokenBlockHistory() {
        return this.blockBrokenHistory;
    }

    public BlockPlacedHistory getPlacedBlockHistory() {
        return this.blockPlacedHistory;
    }

    public OpenedContainerHistory getOpenedContainerHistory() {
        return this.openedContainerHistory;
    }

    public InteractedEntityWithContainerHistory getInteractedEntityWithContainerHistory() {
        return this.interactedEntityWithContainerHistory;
    }

    public void saveAllMaps() {
        this.blockBrokenHistory.saveHistory();
        this.blockPlacedHistory.saveHistory();
        this.openedContainerHistory.saveHistory();
        this.interactedEntityWithContainerHistory.saveHistory();
    }

}