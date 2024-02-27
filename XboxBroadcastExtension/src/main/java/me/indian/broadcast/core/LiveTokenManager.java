package me.indian.broadcast.core;

import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.broadcast.core.exceptions.LiveAuthenticationException;
import me.indian.broadcast.core.models.auth.LiveDeviceCodeResponse;
import me.indian.broadcast.core.models.auth.LiveTokenCache;
import me.indian.broadcast.core.models.auth.LiveTokenResponse;

/**
 * Handle authentication against Microsoft/Live servers and caching of the received tokens
 */
public class LiveTokenManager {
    private final Path cache;
    private final HttpClient httpClient;
    private final Logger logger;

    /**
     * Create an instance of LiveTokenManager
     *
     * @param cache      The directory to store the cached tokens in
     * @param httpClient The HTTP client to use for all requests
     * @param logger     The logger to use for outputting messages
     */
    public LiveTokenManager(final String cache, final HttpClient httpClient, final Logger logger) {
        this.cache = Paths.get(cache, "live_token.json");
        this.httpClient = httpClient;
        this.logger = logger;
    }

    /**
     * Check if the cached token is valid, if it has expired,
     * and we have a refresh the token will be renewed
     *
     * @return true if the token in the cache is valid
     */
    public boolean verifyTokens() {
        final LiveTokenCache tokenCache = this.getCache();

        if (tokenCache.obtainedOn == 0L) {
            return false;
        }

        final long expiry = tokenCache.obtainedOn + (tokenCache.token.expires_in * 1000L);
        final boolean valid = (expiry - System.currentTimeMillis()) > 1000;

        if (!valid) {
            try {
                this.refreshToken();
            } catch (final Exception exception) {
                this.logger.error("Nie udało się odświeżyć tokena&b Xbox Live", exception);
                return false;
            }
        }

        return true;
    }

    /**
     * Delete the cached token file
     */
    public void clearTokenCache() {
        try {
            Files.deleteIfExists(this.cache);
        } catch (final IOException exception) {
            this.logger.error("Failed to delete Live token cache", exception);
        }
    }

    /**
     * Take the stored refresh token and use it to get
     * a new authentication token
     *
     * @throws Exception If the fetching of the token fails
     */
    private void refreshToken() throws Exception {
        final String refreshToken = this.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new Exception("No refresh token");
        }

        final HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(Constants.LIVE_TOKEN_REQUEST)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("scope=" + Constants.SCOPE + "&client_id=" + Constants.AUTH_TITLE + "&grant_type=refresh_token&refresh_token=" + refreshToken))
                .build();

        final LiveTokenResponse tokenResponse = GsonUtil.getGson().fromJson(this.httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString()).body(), LiveTokenResponse.class);

        if (tokenResponse.error != null && !tokenResponse.error.isEmpty()) {
            throw new Exception("Failed to get authentication token: " + tokenResponse.error_description + " (" + tokenResponse.error + ")");
        }

        this.updateCache(tokenResponse);
    }

    /**
     * Fetch the access token from the cache
     *
     * @return The stored access token
     */
    public String getAccessToken() {
        return this.getCache().token.access_token;
    }

    /**
     * Fetch the refresh token from the cache
     *
     * @return The stored refresh token
     */
    private String getRefreshToken() {
        return this.getCache().token.refresh_token;
    }

    /**
     * Start authentication using the device code flow
     * https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-device-code
     *
     * @return The authentication token retrieved
     */
    public Future<String> authDeviceCode() {
        final CompletableFuture<String> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            // Create the initial request for the device code
            final HttpRequest codeRequest = HttpRequest.newBuilder()
                    .uri(Constants.LIVE_DEVICE_CODE_REQUEST)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("scope=" + Constants.SCOPE + "&client_id=" + Constants.AUTH_TITLE + "&response_type=device_code"))
                    .build();

            // Get and parse the response
            final LiveDeviceCodeResponse codeResponse = GsonUtil.getGson().fromJson(this.httpClient.send(codeRequest, HttpResponse.BodyHandlers.ofString()).body(), LiveDeviceCodeResponse.class);

            // Work out the expiry time in ms
            final long expireTime = System.currentTimeMillis() + (codeResponse.expires_in * 1000L);

            // Log the authentication code and link

            this.logger.info("Aby się zalogować, użyj przeglądarki internetowej, otwórz stronę " + codeResponse.verification_uri + "?otc=" + codeResponse.user_code + " i zaloguj w celu uwierzytelnienia.");

            // Loop until the token expires or the user finishes authentication
            while (System.currentTimeMillis() < expireTime) {
                try {
                    // Sleep the thread for the token fetch interval
                    ThreadUtil.sleep(codeResponse.interval);

                    // Create the token request payload
                    final HttpRequest tokenRequest = HttpRequest.newBuilder()
                            .uri(Constants.LIVE_TOKEN_REQUEST)
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofString("device_code=" + codeResponse.device_code + "&client_id=" + Constants.AUTH_TITLE + "&grant_type=urn:ietf:params:oauth:grant-type:device_code"))
                            .build();

                    // Get and parse the response
                    final LiveTokenResponse tokenResponse = GsonUtil.getGson().fromJson(this.httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString()).body(), LiveTokenResponse.class);

                    // Check if we have any errors else the authentication succeeded
                    if (tokenResponse.error != null && !tokenResponse.error.isEmpty()) {
                        // If the error isn't that we are waiting for the user then error out
                        if (!tokenResponse.error.equals("authorization_pending")) {
                            completableFuture.completeExceptionally(new LiveAuthenticationException("Failed to get authentication token while waiting for device: " + tokenResponse.error_description + " (" + tokenResponse.error + ")"));
                            break;
                        }
                    } else {
                        // Update the cache with our token and then return the completable future
                        this.updateCache(tokenResponse);
                        completableFuture.complete(tokenResponse.access_token);
                        break;
                    }
                } catch (final Exception exception) {
                    completableFuture.completeExceptionally(exception);
                }
            }

            // If we have got here and the completable future isn't done then complete with an exception
            if (!completableFuture.isDone()) {
                completableFuture.completeExceptionally(new LiveAuthenticationException("Token kodu urządzenia wygasł przed zakończeniem uwierzytelniania użytkownika"));
            }

            return null;
        });

        return completableFuture;
    }

    /**
     * Read and parse the current cache file
     *
     * @return The parsed cache
     */
    private LiveTokenCache getCache() {
        try {
            return GsonUtil.getGson().fromJson(Files.readString(this.cache), LiveTokenCache.class);
        } catch (final IOException exception) {
            return new LiveTokenCache();
        }
    }

    /**
     * Store updated values into the cache file
     *
     * @param tokenResponse The updated values to store
     */
    private void updateCache(final LiveTokenResponse tokenResponse) {
        try (final FileWriter writer = new FileWriter(this.cache.toString(), StandardCharsets.UTF_8)) {
            Constants.OBJECT_MAPPER.writeValue(writer, new LiveTokenCache(System.currentTimeMillis(), tokenResponse));
        } catch (final IOException exception) {
            this.logger.error("Failed to update Live token cache", exception);
        }
    }
}
