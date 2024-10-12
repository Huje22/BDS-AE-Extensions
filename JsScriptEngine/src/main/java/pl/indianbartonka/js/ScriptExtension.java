package pl.indianbartonka.js;

import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.extension.Extension;
import pl.indianbartonka.js.config.Config;

public class ScriptExtension extends Extension {

    private BDSAutoEnable bdsAutoEnable;
    private Config config;
    private ScriptManager scriptManager;

    @Override
    public void onEnable() {
        this.bdsAutoEnable = this.getBdsAutoEnable();
        this.config = this.createConfig(Config.class, "config");
        this.scriptManager = new ScriptManager(this);

        this.scriptManager.loadScripts();
        this.scriptManager.invokeAllScripts();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public Config getConfig() {
        return this.config;
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
