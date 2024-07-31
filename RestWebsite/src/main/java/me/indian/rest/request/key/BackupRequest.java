package me.indian.rest.request.key;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.watchdog.module.BackupModule;
import me.indian.rest.HttpHandler;
import me.indian.rest.RestWebsite;
import me.indian.rest.component.Info;
import me.indian.rest.util.APIKeyUtil;

public class BackupRequest extends HttpHandler {

    private final RestWebsite restWebsite;
    private final Logger logger;
    private final Javalin app;
    private final BackupModule backupModule;
    private final Gson gson;

    public BackupRequest(final RestWebsite restWebsite) {
        this.restWebsite = restWebsite;
        this.logger = restWebsite.getLogger();
        this.app = this.restWebsite.getApp();
        this.backupModule = restWebsite.getBdsAutoEnable().getWatchDog().getBackupModule();
        this.gson = GsonUtil.getGson();
    }

    @Override
    public void handle(final Javalin app) {
        app.get("/api/backup/", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            if (!APIKeyUtil.isBackupKey(ctx)) return;
            final List<String> backupsNames = this.backupModule.getBackupsNames();

            ctx.status(HttpStatus.OK)
                    .contentType(ContentType.JSON)
                    .result(GsonUtil.getGson().toJson(backupsNames));
        });

        this.app.get("/api/backup/{filename}", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
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
                        ctx.contentType(ContentType.APPLICATION_JSON)
                                .result(this.gson.toJson(new Info("Ten plik już nie istnieje", HttpStatus.NOT_FOUND.getCode())));
                    }
                    return;
                }
            }
            ctx.contentType(ContentType.APPLICATION_JSON)
                    .result(this.gson.toJson(new Info("Nie udało się odnaleźć backup o nazwie: " + filename, HttpStatus.NOT_FOUND.getCode())));
        });
    }
}