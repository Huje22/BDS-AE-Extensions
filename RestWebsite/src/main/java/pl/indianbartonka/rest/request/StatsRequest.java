package pl.indianbartonka.rest.request;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import java.net.HttpURLConnection;
import pl.indianbartonka.bds.player.PlayerStatistics;
import pl.indianbartonka.bds.server.ServerManager;
import pl.indianbartonka.bds.server.stats.StatsManager;
import pl.indianbartonka.bds.util.GsonUtil;
import pl.indianbartonka.util.MessageUtil;
import pl.indianbartonka.rest.HttpHandler;
import pl.indianbartonka.rest.RestWebsite;
import pl.indianbartonka.rest.component.Info;

public class StatsRequest extends HttpHandler {

    private final RestWebsite restWebsite;
    private final ServerManager serverManager;
    private final StatsManager statsManager;
    private final Gson gson;

    public StatsRequest(final RestWebsite restWebsite) {
        this.restWebsite = restWebsite;
        this.serverManager = restWebsite.getBdsAutoEnable().getServerManager();
        this.statsManager = this.serverManager.getStatsManager();
        this.gson = GsonUtil.getGson();
    }

    @Override
    public void handle(final Javalin app) {
        app.get("/api/stats/playtime", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            ctx.contentType(ContentType.APPLICATION_JSON).status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.statsManager.getPlayTime()));
        });

        app.get("/api/stats/deaths", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            ctx.contentType(ContentType.APPLICATION_JSON)
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.statsManager.getDeaths()));
        });

        app.get("/api/stats/players", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            ctx.contentType(ContentType.APPLICATION_JSON)
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.playersJson());
        });

        app.get("/api/stats/block/placed", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            ctx.contentType(ContentType.APPLICATION_JSON)
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.statsManager.getBlockPlaced()));
        });

        app.get("/api/stats/block/broken", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            ctx.contentType(ContentType.APPLICATION_JSON)
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.statsManager.getBlockBroken()));
        });

        app.get("/api/stats/player/name/{player}", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            final String playerName = ctx.pathParam("player");
            final PlayerStatistics player = this.statsManager.getPlayer(playerName);

            if (player != null) {
                ctx.contentType(ContentType.APPLICATION_JSON)
                        .status(HttpURLConnection.HTTP_OK)
                        .result(this.gson.toJson(player));
            } else {
                ctx.contentType(ContentType.APPLICATION_JSON)
                        .result(this.gson.toJson(new Info("Nie udało się odnaleźć gracza o nick '" + playerName + "'", HttpStatus.NOT_FOUND.getCode())));
            }
        });

        app.get("/api/stats/player/xuid/{xuid}", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            try {
                final long xuid = Long.parseLong(ctx.pathParam("xuid"));
                final PlayerStatistics player = this.statsManager.getPlayer(xuid);

                if (player != null) {
                    ctx.contentType(ContentType.APPLICATION_JSON)
                            .status(HttpURLConnection.HTTP_OK)
                            .result(this.gson.toJson(player));
                } else {
                    ctx.contentType(ContentType.APPLICATION_JSON)
                            .result(this.gson.toJson(new Info("Nie udało się odnaleźć gracza o xuid '" + xuid + "'", HttpStatus.NOT_FOUND.getCode())));
                }
            } catch (final Exception exception) {
                ctx.contentType(ContentType.APPLICATION_JSON)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .result(this.gson.toJson(new Info(MessageUtil.getStackTraceAsString(exception), HttpStatus.INTERNAL_SERVER_ERROR.getCode())));
            }
        });
    }

    private String playersJson() {
        final JsonObject json = new JsonObject();
        final JsonArray onlinePlayers = new JsonArray();

        for (final String playerName : this.serverManager.getOnlinePlayers()) {
            onlinePlayers.add(playerName);
        }

        json.add("online", onlinePlayers);

        final JsonArray offlinePlayers = new JsonArray();

        for (final String playerName : this.serverManager.getOfflinePlayers()) {
            offlinePlayers.add(playerName);
        }

        json.add("offline", offlinePlayers);
        return this.gson.toJson(json);
    }
}