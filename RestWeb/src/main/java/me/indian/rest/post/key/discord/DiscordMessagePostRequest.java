package me.indian.rest.post.key.discord;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.discord.DiscordExtension;
import me.indian.discord.jda.DiscordJDA;
import me.indian.discord.webhook.WebHook;
import me.indian.rest.Request;
import me.indian.rest.RestWebsite;
import me.indian.rest.component.discord.DiscordMessagePostData;

import java.net.HttpURLConnection;

public class DiscordMessagePostRequest implements Request {

    private final RestWebsite restWebsite;
    private final DiscordExtension discordExtension;
    private final DiscordJDA discordJDA;
    private final WebHook webHook;
    private final Logger logger;
    private final Javalin app;
    private final Gson gson;

    public DiscordMessagePostRequest(final RestWebsite restWebsite, final DiscordExtension discordExtension, final BDSAutoEnable bdsAutoEnable) {
        this.restWebsite = restWebsite;
        this.discordExtension = discordExtension;
        this.discordJDA = this.discordExtension.getDiscordJDA();
        this.webHook = this.discordExtension.getWebHook();
        this.logger = bdsAutoEnable.getLogger();
        this.app = this.restWebsite.getApp();
        this.gson = GsonUtil.getGson();
    }

    @Override
    public void init() {
        this.app.post("/discord/message/{api-key}", ctx -> {
            this.restWebsite.addRateLimit(ctx);
            if (!this.restWebsite.isCorrectApiKey(ctx)) return;

            final String ip = ctx.ip();
            final String requestBody = ctx.body();
            final DiscordMessagePostData data;

            try {
                data = GsonUtil.getGson().fromJson(requestBody, DiscordMessagePostData.class);
            } catch (final Exception exception) {
                this.restWebsite.incorrectJsonMessage(ctx, exception);
                return;
            }

            if (data.name() == null || data.message() == null) {
                this.restWebsite.incorrectJsonMessage(ctx, this.gson.toJson(data));
                return;
            }

            switch (data.messageType()) {
                case WEBHOOK -> {
                    if (!this.discordExtension.isWebhookEnabled()) {
                        ctx.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(ContentType.TEXT_PLAIN).result("Webhook jest wyłączony");
                        return;
                    }

                    this.webHook.sendMessage(data.message());
                }

                case JDA -> {
                    if (!this.discordExtension.isBotEnabled()) {
                        ctx.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(ContentType.TEXT_PLAIN).result("Bot jest wyłączony");
                        return;
                    }
                    this.discordJDA.sendPlayerMessage(data.name(), data.message());
                }

                default -> {
                    ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.TEXT_PLAIN)
                            .result("'MessageType' (" + data.messageType() + ") jest nie poprawny!");
                    return;
                }
            }


            this.logger.debug("&b" + ip + "&r używa poprawnie endpointu&1 DISCORD/MESSAGE");

            ctx.status(HttpURLConnection.HTTP_NO_CONTENT);
        });
    }
}