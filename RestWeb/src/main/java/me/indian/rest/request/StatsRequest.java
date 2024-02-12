package me.indian.rest.request;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import java.net.HttpURLConnection;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.server.manager.ServerManager;
import me.indian.bds.util.GsonUtil;
import me.indian.rest.HttpHandler;
import me.indian.rest.RestWebsite;

public class StatsRequest extends HttpHandler {

    private final RestWebsite restWebsite;
    private final ServerManager serverManager;
    private final Gson gson;

    public StatsRequest(final RestWebsite restWebsite, final BDSAutoEnable bdsAutoEnable) {
        this.restWebsite = restWebsite;
        this.serverManager = bdsAutoEnable.getServerManager();
        this.gson = GsonUtil.getGson();
    }

    @Override
    public void handle(final Javalin app) {
        app.get("/api/stats/playtime", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType("application/json").status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.serverManager.getStatsManager().getPlayTime()));
        });

        app.get("/api/stats/deaths", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType("application/json")
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.serverManager.getStatsManager().getDeaths()));
        });

        app.get("/api/stats/players", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType("application/json")
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.playersJson());
        });

        app.get("/api/stats/block/placed", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType("application/json")
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.serverManager.getStatsManager().getBlockPlaced()));
        });

        app.get("/api/stats/block/broken", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            ctx.contentType("application/json")
                    .status(HttpURLConnection.HTTP_OK)
                    .result(this.gson.toJson(this.serverManager.getStatsManager().getBlockBroken()));
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