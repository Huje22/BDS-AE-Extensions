package me.indian.host2play.listener;

import me.indian.bds.event.EventHandler;
import me.indian.bds.event.Listener;
import me.indian.bds.event.server.ExtensionEnableEvent;
import me.indian.host2play.Host2PlayExtension;

public class ExtensionEnableListener implements Listener {

    private final Host2PlayExtension host2PlayExtension;

    public ExtensionEnableListener(final Host2PlayExtension host2PlayExtension) {
        this.host2PlayExtension = host2PlayExtension;
    }

    @EventHandler
    private void onExtensionEnable(final ExtensionEnableEvent event) {
        if (event.getExtension() instanceof Host2PlayExtension) {
            this.host2PlayExtension.getBdsAutoEnable().getExtensionManager().disableExtension(this.host2PlayExtension);
        }
    }
}
