package me.indian.broadcast.core;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.mizosoft.methanol.Methanol;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import me.indian.bds.logger.Logger;
import me.indian.broadcast.core.exceptions.SessionCreationException;
import me.indian.broadcast.core.exceptions.SessionUpdateException;
import me.indian.broadcast.core.models.auth.SISUAuthenticationResponse;
import me.indian.broadcast.core.models.auth.XboxTokenInfo;
import me.indian.broadcast.core.models.session.CreateHandleRequest;
import me.indian.broadcast.core.models.session.CreateHandleResponse;
import me.indian.broadcast.core.models.session.SessionRef;
import me.indian.broadcast.core.models.session.SocialSummaryResponse;

/**
 * Simple manager to authenticate and create sessions on Xbox
 */
public abstract class SessionManagerCore {
    protected final HttpClient httpClient;
    protected final Logger logger;
    protected final String cache;
    private final LiveTokenManager liveTokenManager;
    private final XboxTokenManager xboxTokenManager;
    private final FriendManager friendManager;
    protected RtaWebsocketClient rtaWebsocket;
    protected ExpandedSessionInfo sessionInfo;
    protected String lastSessionResponse;

    protected boolean initialized = false;

    /**
     * Create an instance of SessionManager
     *
     * @param cache  The directory to store the cached tokens in
     * @param logger The logger to use for outputting messages
     */
    public SessionManagerCore(final String cache, final Logger logger) {
        this.httpClient = Methanol.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .requestTimeout(Duration.ofMillis(5000))
                .build();

        this.logger = logger;
        this.cache = cache;

        this.liveTokenManager = new LiveTokenManager(cache, this.httpClient, logger);
        this.xboxTokenManager = new XboxTokenManager(cache, this.httpClient, logger);

        this.friendManager = new FriendManager(this.httpClient, logger, this);

        final File directory = new File(cache);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Get the Xbox LIVE friend manager for this session manager
     *
     * @return The friend manager
     */
    public FriendManager friendManager() {
        return this.friendManager;
    }

    /**
     * Get the scheduled thread pool for this session manager
     *
     * @return The scheduled thread pool
     */
    public abstract ScheduledExecutorService scheduledThread();

    /**
     * Get the session ID for this session manager
     *
     * @return The session ID
     */
    public abstract String getSessionId();

    /**
     * Get the logger for this session manager
     *
     * @return The logger
     */
    public Logger logger() {
        return this.logger;
    }

    /**
     * Get the MSA token for the cached user or start the authentication process
     *
     * @return The fetched MSA token
     */
    protected String getMsaToken() {
        if (this.liveTokenManager.verifyTokens()) {
            return this.liveTokenManager.getAccessToken();
        } else {
            try {
                return this.liveTokenManager.authDeviceCode().get();
            } catch (final InterruptedException | ExecutionException exception) {
                this.logger.error("Failed to get authentication token from device code", exception);
                return "";
            }
        }
    }

    /**
     * Get the Xbox token information for the current user
     * If there is no current user then the auto process is started
     *
     * @return The information about the Xbox authentication token including the token itself
     */
    protected XboxTokenInfo getXboxToken() {
        if (xboxTokenManager.verifyTokens()) {
            return xboxTokenManager.getCachedXstsToken();
        } else {
            String msaToken = getMsaToken();
            if (!msaToken.isEmpty()) {
                String deviceToken = xboxTokenManager.getDeviceToken();
                SISUAuthenticationResponse sisuAuthenticationResponse = xboxTokenManager.getSISUToken(msaToken, deviceToken);
                if (sisuAuthenticationResponse != null) {
                    return xboxTokenManager.getXSTSToken(sisuAuthenticationResponse);
                } else {
                    logger.info("SISU authentication response is null, please login again");
                }
            } else {
                logger.info("MSA authentication response is null, please login again");
            }
        }

        liveTokenManager.clearTokenCache();
        return getXboxToken();
    }

    /**
     * Initialize the session manager with the given session information
     *
     * @throws SessionCreationException If the session failed to create either because it already exists or some other reason
     * @throws SessionUpdateException   If the session data couldn't be set due to some issue
     */
    public void init() throws SessionCreationException, SessionUpdateException {
        if (this.initialized) {
            throw new SessionCreationException("Już zainicjowane!");
        }

        this.logger.info("Uruchamiam menedżera sesji...");

        // Make sure we are logged in
        final XboxTokenInfo tokenInfo = this.getXboxToken();
        this.logger.info("Pomyślnie uwierzytelniono jako&b " + tokenInfo.gamertag() + "&r (&d" + tokenInfo.userXUID() + "&r)");

        if (this.handleFriendship()) {
            this.logger.info("Oczekiwanie na przetworzenie przyjaźni...");
            try {
                Thread.sleep(5000); // TODO Do a real callback not just wait
            } catch (final InterruptedException exception) {
                this.logger.error("Nie udało się poczekać na przetworzenie przyjaźni", exception);
            }
        }

        this.logger.info("Tworzenie sesji Xbox LIVE...");

        // Create the session
        this.createSession();

        // Update the presence
        this.updatePresence();

        // Let the user know we are done
        this.logger.info("&aUtworzenie sesji Xbox LIVE powiodło się!");

        this.initialized = true;
    }

    /**
     * Handle the friendship of the current user to the main session if needed
     *
     * @return True if the friendship is being handled, false otherwise
     */
    protected abstract boolean handleFriendship();

    /**
     * Setup a new session and its prerequisites
     *
     * @throws SessionCreationException If the initial creation of the session fails
     * @ If the updating of the session information fails
     */
    private void createSession() throws SessionCreationException, SessionUpdateException {
        // Get the token for authentication
        final XboxTokenInfo tokenInfo = this.getXboxToken();
        final String token = tokenInfo.tokenHeader();

        // We only need a websocket for the primary session manager
        if (this.sessionInfo != null) {
            // Update the current session XUID
            this.sessionInfo.setXuid(tokenInfo.userXUID());

            // Create the RTA websocket connection
            this.setupWebsocket(token);

            try {
                // Wait and get the connection ID from the websocket
                final String connectionId = this.waitForConnectionId().get();

                // Update the current session connection ID
                this.sessionInfo.setConnectionId(connectionId);
            } catch (final InterruptedException | ExecutionException exception) {
                throw new SessionCreationException("Nie można uzyskać identyfikatora połączenia dla sesji: " + exception.getMessage());
            }
        }

        // Push the session information to the session directory
        this.updateSession();

        // Create the session handle request
        final CreateHandleRequest createHandleContent = new CreateHandleRequest(
                1,
                "activity",
                new SessionRef(
                        Constants.SERVICE_CONFIG_ID,
                        "MinecraftLobby",
                        this.getSessionId()
                )
        );

        // Make the request to create the session handle
        final HttpRequest createHandleRequest;
        try {
            createHandleRequest = HttpRequest.newBuilder()
                    .uri(Constants.CREATE_HANDLE)
                    .header("Content-Type", "application/json")
                    .header("Authorization", token)
                    .header("x-xbl-contract-version", "107")
                    .POST(HttpRequest.BodyPublishers.ofString(Constants.OBJECT_MAPPER.writeValueAsString(createHandleContent)))
                    .build();
        } catch (final JsonProcessingException exception) {
            throw new SessionCreationException("Unable to create session handle, error parsing json: " + exception.getMessage());
        }

        // Read the handle response
        final HttpResponse<String> createHandleResponse;
        try {
            createHandleResponse = this.httpClient.send(createHandleRequest, HttpResponse.BodyHandlers.ofString());
            if (this.sessionInfo != null) {
                final CreateHandleResponse parsedResponse = Constants.OBJECT_MAPPER.readValue(createHandleResponse.body(), CreateHandleResponse.class);
                this.sessionInfo.setHandleId(parsedResponse.id());
            }
        } catch (final IOException | InterruptedException exception) {
            throw new SessionCreationException(exception.getMessage());
        }

        this.lastSessionResponse = createHandleResponse.body();

        // Check to make sure the handle was created
        if (createHandleResponse.statusCode() != 200 && createHandleResponse.statusCode() != 201) {
            this.logger.debug("Failed to create session handle '" + this.lastSessionResponse + "' (" + createHandleResponse.statusCode() + ")");
            throw new SessionCreationException("Unable to create session handle, got status " + createHandleResponse.statusCode() + " trying to create");
        }
    }

    /**
     * Update the session information using the stored information
     *
     * @ If the update fails
     */
    protected abstract void updateSession() throws SessionUpdateException;

    /**
     * The internal method for making the web request to update the session
     *
     * @param url  The url to send the PUT request containing the session data
     * @param data The data to update the session with
     * @return The response body from the request
     * @ If the update fails
     */
    protected String updateSessionInternal(final String url, final Object data) throws SessionUpdateException {
        final HttpRequest createSessionRequest;
        try {
            createSessionRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", this.getTokenHeader())
                    .header("x-xbl-contract-version", "107")
                    .PUT(HttpRequest.BodyPublishers.ofString(Constants.OBJECT_MAPPER.writeValueAsString(data)))
                    .build();
        } catch (final JsonProcessingException exception) {
            throw new SessionUpdateException("Nie można zaktualizować informacji o sesji, błąd podczas analizy JSON: " + exception.getMessage());
        }

        final HttpResponse<String> createSessionResponse;
        try {
            createSessionResponse = this.httpClient.send(createSessionRequest, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException exception) {
            throw new SessionUpdateException(exception.getMessage());
        }

        if (createSessionResponse.statusCode() != 200 && createSessionResponse.statusCode() != 201) {
            this.logger.debug("Got update session response: " + createSessionResponse.body());
            throw new SessionUpdateException("Unable to update session information, got status " + createSessionResponse.statusCode() + " trying to update");
        }

        return createSessionResponse.body();
    }

    /**
     * Check the connection to the websocket and if its closed re-open it and re-create the session
     * This should be called before any updates to the session otherwise they might fail
     */
    protected void checkConnection() {
        if (this.rtaWebsocket != null && !this.rtaWebsocket.isOpen()) {
            try {
                this.logger.info("Utracono połączenie z websocketem, ponowne tworzenie sesji...");
                this.createSession();
                this.logger.info("Połączono ponownie!");
            } catch (final SessionCreationException | SessionUpdateException exception) {
                this.logger.error("Sesja jest martwa i wystąpił wyjątek, próbując ją odtworzyć", exception);
            }
        }
    }

    /**
     * Use the data in the cache to get the Xbox authentication header
     *
     * @return The formatted XBL3.0 authentication header
     */
    public String getTokenHeader() {
        return this.getXboxToken().tokenHeader();
    }

    /**
     * Wait for the RTA websocket to receive a connection ID
     *
     * @return The received connection ID
     */
    protected Future<String> waitForConnectionId() {
        final CompletableFuture<String> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            while (this.rtaWebsocket.getConnectionId() == null) {
                Thread.sleep(100);
            }
            completableFuture.complete(this.rtaWebsocket.getConnectionId());

            return null;
        });

        return completableFuture;
    }

    /**
     * Setup the RTA websocket connection
     *
     * @param token The authentication token to use
     */
    protected void setupWebsocket(final String token) {
        this.rtaWebsocket = new RtaWebsocketClient(token, this.logger);
        this.rtaWebsocket.connect();
    }

    /**
     * Stop the current session and close the websocket
     */
    public void shutdown() {
        if (this.rtaWebsocket != null) {
            this.rtaWebsocket.close();
        }
        this.initialized = false;
    }

    /**
     * Update the presence of the current user on Xbox LIVE
     */
    protected void updatePresence() {
        final HttpRequest updatePresenceRequest = HttpRequest.newBuilder()
                .uri(URI.create(Constants.USER_PRESENCE.formatted(this.getXboxToken().userXUID())))
                .header("Content-Type", "application/json")
                .header("Authorization", this.getTokenHeader())
                .header("x-xbl-contract-version", "3")
                .POST(HttpRequest.BodyPublishers.ofString("{\"state\": \"active\"}"))
                .build();

        int heartbeatAfter = 300;
        try {
            final HttpResponse<Void> updatePresenceResponse = this.httpClient.send(updatePresenceRequest, HttpResponse.BodyHandlers.discarding());

            if (updatePresenceResponse.statusCode() != 200) {
                this.logger.error("Failed to update presence, got status " + updatePresenceResponse.statusCode());
            } else {
                // Read X-Heartbeat-After header to get the next time we should update presence
                try {
                    heartbeatAfter = Integer.parseInt(updatePresenceResponse.headers().firstValue("X-Heartbeat-After").orElse("300"));
                } catch (final NumberFormatException exception) {
                    this.logger.debug("Failed to parse heartbeat after header, using default of 300");
                }
            }
        } catch (final IOException | InterruptedException exception) {
            this.logger.error("Failed to update presence", exception);
        }

        // Schedule the next presence update
        this.logger.debug("Aktualizacja obecności udana, planowanie aktualizacji obecności za&a " + heartbeatAfter + "&b sekund");

        this.scheduledThread().schedule(this::updatePresence, heartbeatAfter, TimeUnit.SECONDS);
    }

    /**
     * Get the current follower count for the current user
     *
     * @return The current follower count
     */
    public SocialSummaryResponse socialSummary() {
        final HttpRequest socialSummaryRequest = HttpRequest.newBuilder()
                .uri(Constants.SOCIAL_SUMMARY)
                .header("Authorization", this.getTokenHeader())
                .GET()
                .build();


        try {
            return Constants.OBJECT_MAPPER.readValue(this.httpClient.send(socialSummaryRequest, HttpResponse.BodyHandlers.ofString()).body(), SocialSummaryResponse.class);
        } catch (final IOException | InterruptedException exception) {
            this.logger.error("Unable to get current friend count", exception);
        }

        return new SocialSummaryResponse(-1, -1, false, false, false, false, "", -1, -1, "");
    }
}
