package me.indian.effect;

import me.indian.bds.extension.Extension;
import me.indian.effect.config.EffectsConfig;
import me.indian.effect.listener.PlayerListener;

public class EffectsExtension extends  Extension {

    private EffectsConfig config;

    @Override
    public void onEnable() {
        this.config = this.createConfig(EffectsConfig.class, "config");

        this.getBdsAutoEnable().getEventManager().registerListener(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        if (this.config != null) this.config.save();
    }

    public EffectsConfig getConfig() {
        return this.config;
    }
}