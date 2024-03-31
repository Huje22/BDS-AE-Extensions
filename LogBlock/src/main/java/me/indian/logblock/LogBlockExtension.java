package me.indian.logblock;

import me.indian.bds.extension.Extension;
import me.indian.logblock.command.LogBlockCommand;
import me.indian.logblock.listener.PlayerListener;

public class LogBlockExtension extends Extension {

    private Config config;
    private PlayerListener playerListener;

    @Override
    public void onEnable() {
        this.config = this.createConfig(Config.class, "config");
        this.playerListener = new PlayerListener(this);

        this.getBdsAutoEnable().getEventManager().registerListener(this.playerListener, this);
        this.getBdsAutoEnable().getCommandManager().registerCommand(new LogBlockCommand(this, this.playerListener), this);
    }

    @Override
    public void onDisable() {
        if (this.playerListener != null) {
            this.playerListener.saveAllMaps();
        }
    }

    public Config getConfig() {
        return this.config;
    }
}