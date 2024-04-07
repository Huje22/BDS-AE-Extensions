package me.indian.rest.request.key;

import io.javalin.Javalin;
import me.indian.bds.logger.Logger;
import me.indian.bds.watchdog.module.BackupModule;
import me.indian.rest.HttpHandler;
import me.indian.rest.RestWebsite;

public class ServerLogRequest extends HttpHandler {

    private final RestWebsite restWebsite;
    private final Logger logger;
    private final Javalin app;
    private final BackupModule backupModule;

    public ServerLogRequest(final RestWebsite restWebsite) {
        this.restWebsite = restWebsite;
        this.logger = restWebsite.getLogger();
        this.app = this.restWebsite.getApp();
        this.backupModule = restWebsite.getBdsAutoEnable().getWatchDog().getBackupModule();
    }
    
    
    @Override
    public void handle(Javalin app) {
        super.handle(app);
    }
}
