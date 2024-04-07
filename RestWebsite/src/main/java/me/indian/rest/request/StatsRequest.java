package me.indian.rest.request;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import java.net.HttpURLConnection;
import me.indian.bds.player.PlayerStatistics;
import me.indian.bds.server.ServerManager;
import me.indian.bds.server.stats.StatsManager;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.rest.HttpHandler;
import me.indian.rest.RestWebsite;

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
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType(ContentType.APPLICATION_JSON).status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.statsManager.getPlayTime()));
        });

        app.get("/api/stats/deaths", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType(ContentType.APPLICATION_JSON)
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.statsManager.getDeaths()));
        });

        app.get("/api/stats/players", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType(ContentType.APPLICATION_JSON)
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.playersJson());
        });

        app.get("/api/stats/block/placed", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType(ContentType.APPLICATION_JSON)
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.statsManager.getBlockPlaced()));
        });

        app.get("/api/stats/block/broken", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType(ContentType.APPLICATION_JSON)
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.statsManager.getBlockBroken()));
        });

        app.get("/api/stats/player/name/{player}", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            final String playerName = ctx.pathParam("player");
            final PlayerStatistics player = this.statsManager.getPlayer(playerName);

            if (player != null) {
                ctx.contentType(ContentType.APPLICATION_JSON)
                        .status(HttpURLConnection.HTTP_OK)
                        .result(this.gson.toJson(player));
            } else {
                ctx.contentType(ContentType.APPLICATION_JSON)
                        .status(HttpURLConnection.HTTP_NOT_FOUND)
                        .result(this.gson.toJson("Nie udało się odnaleźć gracza o nick '" + playerName + "'"));
            }
        });

        app.get("/api/stats/player/xuid/{xuid}", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            try {
                final long xuid = Long.parseLong(ctx.pathParam("xuid"));
                final PlayerStatistics player = this.statsManager.getPlayer(xuid);

                if (player != null) {
                    ctx.contentType(ContentType.APPLICATION_JSON)
                            .status(HttpURLConnection.HTTP_OK)
                            .result(this.gson.toJson(player));
                } else {
                    ctx.contentType(ContentType.APPLICATION_JSON)
                            .status(HttpURLConnection.HTTP_NOT_FOUND)
                            .result(this.gson.toJson("Nie udało się odnaleźć gracza o xuid '" + xuid + "'"));
                }
            } catch (final Exception exception) {
                ctx.contentType(ContentType.APPLICATION_JSON)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .result(MessageUtil.getStackTraceAsString(exception));
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