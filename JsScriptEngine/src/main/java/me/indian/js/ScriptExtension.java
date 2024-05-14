package me.indian.js;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;
import me.indian.js.config.Config;


public class ScriptExtension extends Extension {

    private BDSAutoEnable bdsAutoEnable;
    private Config config;
    private Logger logger;
    private ScriptManager scriptManager;



    @Override
    public void onEnable() {
        this.bdsAutoEnable = this.getBdsAutoEnable();
        this.config = createConfig(Config.class, "config");
        this.logger = this.getLogger();
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
