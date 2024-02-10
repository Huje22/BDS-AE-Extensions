package me.indian.rest.request.key;

import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.watchdog.module.BackupModule;
import me.indian.rest.Request;
import me.indian.rest.RestWebsite;
import me.indian.rest.util.APIKeyUtil;

public class BackupRequest implements Request {

    private final RestWebsite restWebsite;
    private final Logger logger;
    private final Javalin app;
    private final BackupModule backupModule;

    public BackupRequest(final RestWebsite restWebsite, final BDSAutoEnable bdsAutoEnable) {
        this.restWebsite = restWebsite;
        this.logger = restWebsite.getLogger();
        this.app = this.restWebsite.getApp();
        this.backupModule = bdsAutoEnable.getWatchDog().getBackupModule();
    }

    @Override
    public void init() {
        this.app.get("/api/{api-key}/backup/", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            if (!APIKeyUtil.isBackupKey(ctx)) return;
            final List<String> backupsNames = this.backupModule.getBackupsNames();

            ctx.status(HttpStatus.OK)
                    .contentType(ContentType.JSON)
                    .result(GsonUtil.getGson().toJson(backupsNames));
        });

        this.app.get("/api/{api-key}/backup/{filename}", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            if (!APIKeyUtil.isBackupKey(ctx)) return;

            final String filename = ctx.pathParam("filename");
            final String ip = ctx.ip();

            for (final Path path : this.backupModule.getBackups()) {
                final String fileName = path.getFileName().toString();
                if (filename.equalsIgnoreCase(fileName)) {
                    final File file = new File(path.toString());
                    if (file.exists()) {
                        ctx.res().setHeader("Content-Disposition", "attachment; filename=" + filename);
                        ctx.res().setHeader("Content-Type", "application/zip");
                        ctx.res().setHeader("Content-Length", String.valueOf(file.length()));
                        this.logger.info("&b" + ip + "&r pobiera&3 " + filename);

                        ctx.status(HttpStatus.OK).result(new FileInputStream(file));

                    } else {
                        ctx.status(HttpStatus.NOT_FOUND).contentType(ContentType.TEXT_PLAIN)
                                .result("Nie udało nam się odnaleźć pliku tego backup ponieważ już on nie istneieje");
                    }
                    return;
                }
            }
            ctx.status(HttpStatus.NOT_FOUND).contentType(ContentType.TEXT_PLAIN)
                    .result("Nie udało się odnaleźć backup o nazwie: " + filename);
        });
    }
}