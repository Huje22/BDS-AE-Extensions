package me.indian.host2play;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.CommandManager;
import me.indian.bds.event.EventManager;
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;
import me.indian.rest.RestWebsite;


public class Host2PlayExtension extends Extension {

    private BDSAutoEnable bdsAutoEnable;
    private Logger logger;



    @Override
    public void onEnable() {
        this.bdsAutoEnable = this.getBdsAutoEnable();
        this.logger = this.getLogger();


        final CommandManager commandManager = this.bdsAutoEnable.getCommandManager();
        final EventManager eventManager = this.bdsAutoEnable.getEventManager();


        final RestWebsite restWebsite = (RestWebsite) this.bdsAutoEnable.getExtensionLoader().getExtension("RestWebsite");

        if (restWebsite != null) {
            if (restWebsite.isEnabled()) {
//                restWebsite.register(           );


            }
        }
    }




}