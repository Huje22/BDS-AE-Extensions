package pl.indianbartonka.effect;

import pl.indianbartonka.bds.extension.Extension;
import pl.indianbartonka.effect.config.EffectsConfig;
import pl.indianbartonka.effect.listener.PlayerListener;

public class EffectsExtension extends Extension {

    private EffectsConfig config;

    @Override
    public void onEnable() {
        this.config = this.createConfig(EffectsConfig.class, "config");

        this.getBdsAutoEnable().getEventManager().registerListener(new PlayerListener(this), this);
    }

    public EffectsConfig getConfig() {
        return this.config;
    }
}