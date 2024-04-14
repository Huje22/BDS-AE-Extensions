package me.indian.rest.request.key;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.DefaultsVariables;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.rest.HttpHandler;
import me.indian.rest.RestWebsite;
import me.indian.rest.component.Info;
import me.indian.rest.util.APIKeyUtil;

public class ServerLogRequest extends HttpHandler {

    private final RestWebsite restWebsite;
    private final Logger logger;
    private final Javalin app;
    private final Gson gson;
    private final String logsDir;

    public ServerLogRequest(final RestWebsite restWebsite) {
        this.restWebsite = restWebsite;
        this.logger = restWebsite.getLogger();
        this.app = this.restWebsite.getApp();
        this.gson = GsonUtil.getGson();
        this.logsDir = DefaultsVariables.getLogsDir();
    }

    @Override
    public void handle() {
        this.app.get("/api/{api-key}/log", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            if (!APIKeyUtil.isLogKey(ctx)) return;

            ctx.status(HttpStatus.OK)
                    .contentType(ContentType.JSON)
                    .result(GsonUtil.getGson().toJson(this.getLogsNames()));

        });

        this.app.get("/api/{api-key}/log/{log}", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            if (!APIKeyUtil.isLogKey(ctx)) return;

            final String logName = ctx.pathParam("log");
            final String ip = ctx.ip();

            if (logName.equals("latest")) {
                ctx.status(HttpStatus.OK)
                        .contentType(ContentType.TEXT_PLAIN)
                        .result(MessageUtil.listToSpacedString(Files.readAllLines(this.logger.getLogFile().toPath())));

                this.logger.info("&b" + ip + "&r pobiera&3 " + logName);
                return;
            }

            final File file = new File(this.logsDir + File.separator + logName);

            if (file.exists()) {
                ctx.status(HttpStatus.OK)
                        .contentType(ContentType.TEXT_PLAIN)
                        .result(MessageUtil.listToSpacedString(Files.readAllLines(file.toPath())));

                this.logger.info("&b" + ip + "&r pobiera&3 " + logName);
            } else {
                this.logger.error("&b" + ip + "&r próbuje pobrać log o nazwie:&1 " + logName + "&r lecz takiego nie ma");
                ctx.contentType(ContentType.APPLICATION_JSON)
                        .result(this.gson.toJson(new Info("Nie odnaleziono logu o nazwie: " + logName, HttpStatus.NOT_FOUND.getCode())));
            }
        });
    }

    private List<String> getLogsNames() {
        final File[] logsFiles = new File(this.logsDir).listFiles(log -> log.getName().endsWith(".log"));
        final List<String> list = new ArrayList<>();

        list.add("latest");

        if (logsFiles == null) return list;

        for (final File file : logsFiles) {
            list.add(file.getName());
        }

        return list;
    }
}