package me.indian.rest.post.key;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.GsonUtil;
import me.indian.rest.Request;
import me.indian.rest.RestWebsite;
import me.indian.rest.component.CommandPostData;

import java.net.HttpURLConnection;

public class CommandPostRequest implements Request {

    private final RestWebsite restWebsite;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final Javalin app;
    private final Gson gson;

    public CommandPostRequest(final RestWebsite restWebsite, final BDSAutoEnable bdsAutoEnable) {
        this.restWebsite = restWebsite;
        this.bdsAutoEnable = bdsAutoEnable;
        this.logger = this.bdsAutoEnable.getLogger();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.app = this.restWebsite.getApp();
        this.gson = GsonUtil.getGson();
    }

    @Override
    public void init() {
        this.app.post("/command/{api-key}", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            if (!this.restWebsite.isCorrectApiKey(ctx)) return;

            final String ip = ctx.ip();
            final String requestBody = ctx.body();
            final CommandPostData data;

            try {
                data = GsonUtil.getGson().fromJson(requestBody, CommandPostData.class);
            } catch (final Exception exception) {
                this.restWebsite.incorrectJsonMessage(ctx, exception);
                return;
            }

            final String command = data.command();

            if (command == null) {
                this.restWebsite.incorrectJsonMessage(ctx, GsonUtil.getGson().toJson(data));
                return;
            }

            if (!this.serverProcess.isEnabled()) {
                ctx.status(HttpStatus.SERVICE_UNAVAILABLE).result("Server jest wyłączony");
                return;
            }

            this.logger.debug("&b" + ip + "&r używa poprawnie endpointu&1 COMMAND");
            this.logger.print(command);

            ctx.contentType(ContentType.APPLICATION_JSON).status(HttpURLConnection.HTTP_OK)
                    .result("Ostatnia linia z konsoli: " + this.serverProcess.commandAndResponse(command));
        });
    }
}