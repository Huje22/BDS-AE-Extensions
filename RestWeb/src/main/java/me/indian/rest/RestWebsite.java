package me.indian.rest;

import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.util.RateLimiter;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.discord.DiscordExtension;
import me.indian.rest.config.RestApiConfig;
import me.indian.rest.post.key.CommandPostRequest;
import me.indian.rest.post.key.PlayerInfoPostRequest;
import me.indian.rest.post.key.discord.DiscordMessagePostRequest;
import me.indian.rest.request.StatsRequest;
import me.indian.rest.request.key.BackupRequest;
import me.indian.rest.util.HTMLUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class RestWebsite extends Extension {

    private BDSAutoEnable bdsAutoEnable;
    private Javalin app;
    private RateLimiter limiter;
    private Logger logger;
    private RestApiConfig config;
    private List<Request> requests;
    private File htmlFile;
    private String htmlFileContent;
    private DiscordExtension discordExtension;

    @Override
    public void onLoad() {
        this.bdsAutoEnable = this.getBdsAutoEnable();
        this.app = Javalin.create();
        this.limiter = new RateLimiter(TimeUnit.MINUTES);
        this.logger = this.bdsAutoEnable.getLogger();
        this.config = this.createConfig(RestApiConfig.class, "config");
        this.requests = new ArrayList<>();
        this.htmlFile = new File(this.getDataFolder(), "Website.html");

        this.createHTMLFile();
        this.refreshFileContent();
        this.requests.add(new StatsRequest(this, this.bdsAutoEnable));
        this.requests.add(new BackupRequest(this, this.bdsAutoEnable));
        this.requests.add(new CommandPostRequest(this, this.bdsAutoEnable));
        this.requests.add(new PlayerInfoPostRequest(this, this.bdsAutoEnable));
    }

    @Override
    public void onEnable() {
        if (!this.config.isEnabled()) {
            this.logger.debug("&bRest API&r jest wyłączone");
            return;
        }
        final Extension extension = this.bdsAutoEnable.getExtensionLoader().getExtension("DiscordExtension");
        if (extension != null) {
            this.discordExtension = (DiscordExtension) extension;
            this.logger.info("Znaleziono&b " + extension.getName());
            this.requests.add(new DiscordMessagePostRequest(this, this.discordExtension, this.bdsAutoEnable));
        }

        try {
            this.app.start(this.config.getPort());
            this.app.after(ctx -> ctx.res().setCharacterEncoding("UTF-8"));

            this.app.get("/", ctx -> {
                if (!this.htmlFile.exists()) this.createHTMLFile();

                ctx.contentType(ContentType.TEXT_HTML)
                        .result(this.htmlFileContent
                                .replaceAll("<online>", String.valueOf(this.bdsAutoEnable.getServerManager().getOnlinePlayers().size()))
                                .replaceAll("<max>", String.valueOf(this.bdsAutoEnable.getServerProperties().getMaxPlayers()))
                                .replaceAll("<online-with-stats>", HTMLUtil.getOnlineWithStats(this.bdsAutoEnable))
                        );
            });

            for (final Request request : this.requests) {
                request.init();
            }

            this.logger.info("Uruchomiono strone z rest api na porcie:&b " + this.config.getPort());
        } catch (final Exception exception) {
            this.logger.error("Nie udało się uruchomić strony z&b Rest API", exception);
        }
    }

    @Override
    public void onDisable() {
        if (this.config != null) this.config.save();
        if (this.app != null) this.app.stop();
    }

    public boolean isCorrectApiKey(final Context ctx) {
        final String apiKey = ctx.pathParam("api-key");
        final String ip = ctx.ip();

        if (!this.config.getApiKeys().contains(apiKey)) {
            ctx.status(HttpStatus.UNAUTHORIZED).contentType(ContentType.APPLICATION_JSON)
                    .result(GsonUtil.getGson().toJson("Klucz API \"" + apiKey + "\" nie jest obsługiwany"));

            this.logger.debug("&b" + ip + "&r używa niepoprawnego klucza autoryzacji&c " + apiKey);
            return false;
        }
        return true;
    }

    public void addRateLimit(final Context ctx) {
        this.limiter.incrementCounter(ctx, this.config.getRateLimit());
    }

    public void incorrectJsonMessage(final Context ctx, final String json) {
        this.incorrectJsonMessage(ctx, json, null);
    }

    public void incorrectJsonMessage(final Context ctx) {
        this.incorrectJsonMessage(ctx, (Exception) null);
    }

    public void incorrectJsonMessage(final Context ctx, final String json, final @Nullable Exception exception) {
        final String ip = ctx.ip();

        this.logger.debug("&b" + ip + "&r wysła niepoprawny json&1 " + json.replaceAll("\n", ""), exception);
        ctx.status(HttpStatus.BAD_REQUEST).result("Niepoprawny Json! " + json.replaceAll("\n", ""));
    }

    public void incorrectJsonMessage(final Context ctx, final @Nullable Exception exception) {
        final String ip = ctx.ip();
        final String requestBody = ctx.body();

        this.logger.debug("&b" + ip + "&r wysła niepoprawny json&1 " + requestBody.replaceAll("\n", ""), exception);
        ctx.status(HttpStatus.BAD_REQUEST).result("Niepoprawny Json! " + requestBody.replaceAll("\n", ""));
    }

    private void createHTMLFile() {
        try {
            if (!this.htmlFile.exists()) {
                if (!this.htmlFile.createNewFile()) {
                    this.logger.error("Nie można utworzyć&b Website.html");
                }
            }

            if (Files.size(this.htmlFile.toPath()) == 0) {
                try (final FileWriter writer = new FileWriter(this.htmlFile)) {
                    writer.write(HTMLUtil.getExampleBody());
                }
            }
        } catch (final Exception exception) {
            this.logger.error("Nie udało się utworzyć&b Website.html", exception);
        }
    }

    private void refreshFileContent() {
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!RestWebsite.this.htmlFile.exists()) RestWebsite.this.createHTMLFile();

                try {
                    RestWebsite.this.htmlFileContent = MessageUtil.listToSpacedString(Files.readAllLines(RestWebsite.this.htmlFile.toPath()));
                } catch (final Exception exception) {
                    RestWebsite.this.htmlFileContent = "Strona odświeża<br><br> " + MessageUtil.getStackTraceAsString(exception);
                }
            }
        };

        final long minute = MathUtil.minutesTo(1, TimeUnit.MILLISECONDS);
        new Timer("Refresh File Content", true).scheduleAtFixedRate(task, 0, minute);
    }

    public Javalin getApp() {
        return this.app;
    }
}