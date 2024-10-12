package pl.indianbartonka.rest.request;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import pl.indianbartonka.bds.server.ServerManager;
import pl.indianbartonka.bds.server.stats.StatsManager;
import pl.indianbartonka.bds.util.GsonUtil;
import pl.indianbartonka.util.MessageUtil;
import pl.indianbartonka.bds.util.geyser.GeyserUtil;
import pl.indianbartonka.rest.HttpHandler;
import pl.indianbartonka.rest.RestWebsite;
import pl.indianbartonka.rest.component.Info;

public class SkinRequest extends HttpHandler {

    private final RestWebsite restWebsite;
    private final ServerManager serverManager;
    private final StatsManager statsManager;
    private final Gson gson;

    public SkinRequest(final RestWebsite restWebsite) {
        this.restWebsite = restWebsite;
        this.serverManager = restWebsite.getBdsAutoEnable().getServerManager();
        this.statsManager = this.serverManager.getStatsManager();
        this.gson = GsonUtil.getGson();
    }

    @Override
    public void handle(final Javalin app) {
        app.get("/api/skin/head/xuid/{xuid}", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            try {
                ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN)
                        .result(GeyserUtil.getBedrockSkinHead(Long.parseLong(ctx.pathParam("xuid"))));
            } catch (final Exception exception) {
                this.handleException(ctx, exception);
            }
        });

        app.get("/api/skin/head/name/{name}", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            try {
                ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN)
                        .result(GeyserUtil.getBedrockSkinHead(this.statsManager.getXuidByName(ctx.pathParam("name"))));
            } catch (final Exception exception) {
                this.handleException(ctx, exception);
            }
        });

        app.get("/api/skin/body/xuid/{xuid}", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            try {
                ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN)
                        .result(GeyserUtil.getBedrockSkinBody(Long.parseLong(ctx.pathParam("xuid"))));
            } catch (final Exception exception) {
                this.handleException(ctx, exception);
            }
        });

        app.get("/api/skin/body/name/{name}", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            try {
                ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN)
                        .result(GeyserUtil.getBedrockSkinBody(this.statsManager.getXuidByName(ctx.pathParam("name"))));
            } catch (final Exception exception) {
                this.handleException(ctx, exception);
            }
        });
    }

    private void handleException(final Context ctx, final Exception exception) {
        ctx.contentType(ContentType.APPLICATION_JSON)
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .result(this.gson.toJson(new Info(MessageUtil.getStackTraceAsString(exception), HttpStatus.INTERNAL_SERVER_ERROR.getCode())));
    }
}