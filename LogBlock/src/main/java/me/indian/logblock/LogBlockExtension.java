package me.indian.logblock;

import me.indian.bds.extension.Extension;
import me.indian.logblock.command.LogBlockCommand;
import me.indian.logblock.history.HistoryManager;
import me.indian.logblock.listener.PlayerListener;

public class LogBlockExtension extends Extension {

    private Config config;
    private HistoryManager historyManager;

    @Override
    public void onEnable() {
        this.config = this.createConfig(Config.class, "config");
        this.historyManager = new HistoryManager(this);

        this.getBdsAutoEnable().getEventManager().registerListener(new PlayerListener(this), this);
        this.getBdsAutoEnable().getCommandManager().registerCommand(new LogBlockCommand(this), this);
    }

    @Override
    public void onDisable() {
        if (this.historyManager != null) {
            this.historyManager.saveAllMaps();
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public HistoryManager getHistoryManager() {
        return this.historyManager;
    }

    public void reloadConfig() {
        this.config = (Config) this.config.load(true);
    }
}