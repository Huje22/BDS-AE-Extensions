package pl.indianbartonka.rest.post.key;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import java.net.HttpURLConnection;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.bds.server.ServerProcess;
import pl.indianbartonka.bds.util.GsonUtil;
import pl.indianbartonka.rest.HttpHandler;
import pl.indianbartonka.rest.RestWebsite;
import pl.indianbartonka.rest.component.Info;
import pl.indianbartonka.rest.component.PlayerPostData;
import pl.indianbartonka.rest.util.APIKeyUtil;

public class PlayerInfoPostRequest extends HttpHandler {

    private final RestWebsite restWebsite;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ServerProcess serverProcess;
    private final Gson gson;

    public PlayerInfoPostRequest(final RestWebsite restWebsite) {
        this.restWebsite = restWebsite;
        this.bdsAutoEnable = restWebsite.getBdsAutoEnable();
        this.logger = this.restWebsite.getLogger();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();

        this.gson = GsonUtil.getGson();
    }


    @Override
    public void handle(final Javalin app) {
        app.post("/playerInfo", ctx -> {
            if (this.restWebsite.addRateLimit(ctx)) return;
            if (!APIKeyUtil.isServerKey(ctx)) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                return;
            }

            final String ip = ctx.ip();
            final String requestBody = ctx.body();

            System.out.println(requestBody);

            final PlayerPostData playerPostData;

            try {
                playerPostData = GsonUtil.getGson().fromJson(requestBody, PlayerPostData.class);
            } catch (final Exception exception) {
                this.restWebsite.incorrectJsonMessage(ctx, exception);
                return;
            }

            //TODO:Dokończyć to

            if (!this.serverProcess.isEnabled()) {
                ctx.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(ContentType.APPLICATION_JSON)
                        .result(this.gson.toJson(new Info("Server jest wyłączony", HttpStatus.SERVICE_UNAVAILABLE.getCode())));
                ;
                return;
            }

            this.logger.debug("&b" + ip + "&r używa poprawnie endpointu&1 PLAYERINFO");
            ctx.status(HttpURLConnection.HTTP_NO_CONTENT);
        });
    }
}