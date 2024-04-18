package me.indian.rest.post.key;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.GsonUtil;
import me.indian.rest.HttpHandler;
import me.indian.rest.RestWebsite;
import me.indian.rest.component.CommandPostData;
import me.indian.rest.component.Info;
import me.indian.rest.util.APIKeyUtil;

public class CommandPostRequest extends HttpHandler {

    private final RestWebsite restWebsite;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final Gson gson;

    public CommandPostRequest(final RestWebsite restWebsite) {
        this.restWebsite = restWebsite;
        this.bdsAutoEnable = restWebsite.getBdsAutoEnable();
        this.logger = this.restWebsite.getLogger();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.gson = GsonUtil.getGson();
    }

    @Override
    public void handle(final Javalin app) {
        app.post("/command", ctx -> {
             if(this.restWebsite.addRateLimit(ctx)) return;
            if (!APIKeyUtil.isServerKey(ctx)) return;

            final String ip = ctx.ip();
            final String requestBody = ctx.body();
            final CommandPostData data;

            try {
                data = this.gson.fromJson(requestBody, CommandPostData.class);
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
                ctx.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(ContentType.APPLICATION_JSON)
                        .result(this.gson.toJson(new Info("Server jest wyłączony", HttpStatus.SERVICE_UNAVAILABLE.getCode())));
                return;
            }

            this.logger.debug("&b" + ip + "&r używa poprawnie endpointu&1 COMMAND");
            this.logger.print(command);

            ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON)
                    .result(this.gson.toJson(new Info(this.serverProcess.commandAndResponse(command), HttpStatus.OK.getCode())));
        });
    }
}