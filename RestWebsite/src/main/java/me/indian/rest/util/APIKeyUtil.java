package me.indian.rest.util;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Arrays;
import java.util.List;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.rest.RestWebsite;
import me.indian.rest.component.Info;
import me.indian.rest.config.APIKeyConfig;
import me.indian.rest.config.RestApiConfig;

public final class APIKeyUtil {

    private static RestWebsite RESTWEBSITE;
    private static RestApiConfig CONFIG;
    private static Logger LOGGER;

    private APIKeyUtil() {
    }

    public static void init(final RestWebsite restWebsite) {
        RESTWEBSITE = restWebsite;
        CONFIG = restWebsite.getConfig();
        LOGGER = restWebsite.getLogger();
        generateMissingKeys();
    }

    private static void generateMissingKeys() {
        final APIKeyConfig apiKeyConfig = CONFIG.getAPIKeys();

        if (apiKeyConfig == null) {
            CONFIG.setApiKeyConfig(new APIKeyConfig());
            CONFIG.save();
            generateMissingKeys();
            return;
        }

        final List<List<String>> keys = Arrays.asList(apiKeyConfig.getPowerful(), apiKeyConfig.getServer(),
                apiKeyConfig.getBackup(), apiKeyConfig.getLog());

        for (final List<String> list : keys) {
            if (list.isEmpty()) {
                for (int i = 0; i < 4; i++) {
                    list.add(MessageUtil.generateCode(8));
                }
            }
        }
        CONFIG.save();
    }

    //TODO: Zmienić system autoryzacji 
    public static boolean isCorrectCustomKey(final Context ctx, final List<String> keys) {
        if (isPowerfulKey(ctx)) return true;
        final String apiKey = ctx.pathParam("api-key");
        final String ip = ctx.ip();

        if (!keys.contains(apiKey)) {
            ctx.status(HttpStatus.UNAUTHORIZED).contentType(ContentType.APPLICATION_JSON)
                    .result(GsonUtil.getGson().toJson(new Info("Klucz API " + apiKey + " nie jest obsługiwany", HttpStatus.UNAUTHORIZED.getCode())));

            LOGGER.debug("&b" + ip + "&r używa niepoprawnego klucza autoryzacji&c " + apiKey);
            return false;
        }
        return true;



        String authorization = ctx.header("Authorization");

        if (authorization == null || authorization.isEmpty()) {
            ctx.status(401).result("Unauthorized: No Authorization header provided");
        } else {
            // Tutaj możesz dodać logikę weryfikacji klucza API
            if (isValidApiKey(authorization)) {
                ctx.result("API Key is valid");
            } else {
                ctx.status(403).result("Forbidden: Invalid API Key");
            }
        }
    }


    public static boolean isServerKey(final Context ctx) {
        return isCorrectCustomKey(ctx, CONFIG.getAPIKeys().getServer());
    }

    public static boolean isBackupKey(final Context ctx) {
        return isCorrectCustomKey(ctx, CONFIG.getAPIKeys().getBackup());
    }

    public static boolean isLogKey(final Context ctx){
        return isCorrectCustomKey(ctx, CONFIG.getAPIKeys().getLog());
    }

    public static boolean isPowerfulKey(final Context ctx) {
        return CONFIG.getAPIKeys().getPowerful().contains(ctx.pathParam("api-key"));
    }
}
