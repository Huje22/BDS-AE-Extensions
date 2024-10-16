package pl.indianbartonka.rest;

import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.http.util.RateLimiter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.extension.Extension;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.bds.util.GsonUtil;
import pl.indianbartonka.util.MessageUtil;
import pl.indianbartonka.rest.component.Info;
import pl.indianbartonka.rest.config.RestApiConfig;
import pl.indianbartonka.rest.post.key.CommandPostRequest;
import pl.indianbartonka.rest.post.key.PlayerInfoPostRequest;
import pl.indianbartonka.rest.request.SkinRequest;
import pl.indianbartonka.rest.request.StatsRequest;
import pl.indianbartonka.rest.request.key.BackupRequest;
import pl.indianbartonka.rest.request.key.ServerLogRequest;
import pl.indianbartonka.rest.util.APIKeyUtil;
import pl.indianbartonka.rest.util.HTMLUtil;
import org.jetbrains.annotations.Nullable;

public class RestWebsite extends Extension {

    private BDSAutoEnable bdsAutoEnable;
    private Javalin app;
    private RateLimiter limiter;
    private Logger logger;
    private RestApiConfig config;
    private Set<HttpHandler> httpHandlers;
    private File htmlFile;
    private String htmlFileContent;

    @Override
    public void onLoad() {
        this.bdsAutoEnable = this.getBdsAutoEnable();
        this.app = Javalin.create(config -> {
            config.router.ignoreTrailingSlashes = true;
            config.useVirtualThreads = true;
            config.staticFiles.add(this.getWebDir(), Location.EXTERNAL);
        });
        //TODO: Dodać obsługe ssl https://javalin.io/tutorials/javalin-ssl-tutorial#2-add-the-javalin-ssl-dependency

        this.limiter = new RateLimiter(TimeUnit.MINUTES);
        this.logger = this.getLogger();
        this.config = this.createConfig(RestApiConfig.class, "config");
        this.httpHandlers = new HashSet<>();
        this.htmlFile = new File(this.getWebDir(), "Website.html");

        this.createHTMLFile();
        this.refreshFileContent();
    }

    @Override
    public void onEnable() {
        if (!this.config.isEnabled()) {
            this.logger.debug("&bRest API&r jest wyłączone");
            return;
        }

        APIKeyUtil.init(this);

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

            this.app.error(404, ctx -> ctx.status(HttpStatus.NOT_FOUND)
                    .contentType(ContentType.APPLICATION_JSON)
                    .result(GsonUtil.getGson().toJson(new Info("Nie odnaleziono żądanego zasobu", HttpStatus.NOT_FOUND.getCode()))));

            this.register(new StatsRequest(this));
            this.register(new SkinRequest(this));
            this.register(new BackupRequest(this));
            this.register(new ServerLogRequest(this));
            this.register(new CommandPostRequest(this));
            this.register(new PlayerInfoPostRequest(this));

            this.logger.info("Uruchomiono strone z rest api na porcie:&b " + this.config.getPort());
        } catch (final Exception exception) {
            this.logger.error("Nie udało się uruchomić strony z&b Rest API", exception);
        }
    }

    @Override
    public void onDisable() {
        if (this.app != null) this.app.stop();
    }

    public <T extends HttpHandler> void register(final T httpHandler) {
        this.httpHandlers.add(httpHandler);
        httpHandler.handle();
        httpHandler.handle(this.app);
        this.logger.debug("Zarejestrowano Handler HTTP:&b " + httpHandler.getClass().getSimpleName());
    }

    public boolean addRateLimit(final Context ctx) {
        final String ip = ctx.ip();
        final int limit = this.config.getRateLimit();
        if (this.config.getWhitelistedIP().contains(ip)) return false;

        try {
            this.limiter.incrementCounter(ctx, limit);
        } catch (final Exception exception) {
            this.logger.alert("IP&b " + ip + "&c przekracza limit&1 " + limit + "&c zapytań na&b minute&c!");
            ctx.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(ContentType.APPLICATION_JSON)
                    .result(GsonUtil.getGson().toJson(new Info("Osiągnięto limit (" + limit + ") zapytań na minute!", HttpStatus.TOO_MANY_REQUESTS.getCode())));
            return true;
        }
        return false;
    }

    public void incorrectJsonMessage(final Context ctx, final String json) {
        this.incorrectJsonMessage(ctx, json, null);
    }

    public void incorrectJsonMessage(final Context ctx) {
        this.incorrectJsonMessage(ctx, (Exception) null);
    }

    public void incorrectJsonMessage(final Context ctx, final String json, final @Nullable Exception exception) {
        final String ip = ctx.ip();

        ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON)
                .result(GsonUtil.getGson().toJson(new Info("Niepoprawny Json! " + json.replaceAll("\n", ""), HttpStatus.BAD_REQUEST.getCode())));
        this.logger.debug("&b" + ip + "&r wysyła niepoprawny json&1 " + json.replaceAll("\n", ""), exception);
    }

    public void incorrectJsonMessage(final Context ctx, final @Nullable Exception exception) {
        final String ip = ctx.ip();
        final String requestBody = ctx.body();

        this.logger.debug("&b" + ip + "&r wysyła niepoprawny json&1 " + requestBody.replaceAll("\n", ""), exception);
        ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON)
                .result(GsonUtil.getGson().toJson(new Info("Niepoprawny Json! " + requestBody.replaceAll("\n", ""), HttpStatus.BAD_REQUEST.getCode())));
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

        final long minute = DateUtil.minutesTo(1, TimeUnit.MILLISECONDS);
        new Timer("Refresh File Content", true).scheduleAtFixedRate(task, 0, minute);
    }

    public void reloadConfig() {
        this.config = (RestApiConfig) this.config.load(true);
    }

    public String getWebDir() {
        final String webDir = this.getDataFolder() + File.separator + "web";
        try {
            Files.createDirectories(Path.of(webDir));
        } catch (final IOException exception) {
            this.logger.critical("&cNie udało się utworzyć miejsca z plikami strony!");
            throw new RuntimeException(exception);
        }
        return webDir;
    }

    public Javalin getApp() {
        return this.app;
    }

    public RestApiConfig getConfig() {
        return this.config;
    }
}
