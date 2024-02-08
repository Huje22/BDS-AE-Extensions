package me.indian.broadcast.core;


import com.fasterxml.jackson.core.JsonProcessingException;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.ThreadUtil;
import me.indian.broadcast.config.FriendSyncConfig;
import me.indian.broadcast.core.exceptions.SessionCreationException;
import me.indian.broadcast.core.exceptions.SessionUpdateException;
import me.indian.broadcast.core.models.session.CreateSessionRequest;
import me.indian.broadcast.core.models.session.CreateSessionResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

/**
 * Simple manager to authenticate and create sessions on Xbox
 */
public class SessionManager extends SessionManagerCore {
    private final ScheduledExecutorService scheduledThreadPool;
    private final Map<String, SubSessionManager> subSessionManagers;

    private FriendSyncConfig friendSyncConfig;
    private Runnable restartCallback;

    /**
     * Create an instance of SessionManager
     *
     * @param cache  The directory to store the cached tokens in
     * @param logger The logger to use for outputting messages
     */
    public SessionManager(final String cache, final Logger logger) {
        super(cache, logger);
        this.scheduledThreadPool = Executors.newScheduledThreadPool(5, new ThreadUtil("MCXboxBroadcast"));
        this.subSessionManagers = new HashMap<>();
    }

    @Override
    public ScheduledExecutorService scheduledThread() {
        return this.scheduledThreadPool;
    }

    @Override
    public String getSessionId() {
        return this.sessionInfo.getSessionId();
    }

    /**
     * Get the current session information
     *
     * @return The current session information
     */
    public ExpandedSessionInfo sessionInfo() {
        return this.sessionInfo;
    }

    /**
     * Initialize the session manager with the given session information
     *
     * @param sessionInfo      The session information to use
     * @param friendSyncConfig The friend sync configuration to use
     * @throws SessionCreationException If the session failed to create either because it already exists or some other reason
     * @throws SessionUpdateException   If the session data couldn't be set due to some issue
     */
    public void init(final SessionInfo sessionInfo, final FriendSyncConfig friendSyncConfig) throws SessionCreationException, SessionUpdateException {
        // Set the internal session information based on the session info
        this.sessionInfo = new ExpandedSessionInfo("", "", sessionInfo);

        super.init();

        // Set up the auto friend sync
        this.friendSyncConfig = friendSyncConfig;
        this.friendManager().initAutoFriend(friendSyncConfig);

        // Load sub-sessions from cache
        List<String> subSessions = new ArrayList<>();
        try {
            subSessions = Arrays.asList(Constants.OBJECT_MAPPER.readValue(Paths.get(this.cache, "sub_sessions.json").toFile(), String[].class));
        } catch (final IOException ignored) {
        }

        // Create the sub-session manager for each sub-session
        for (final String subSession : subSessions) {
            try {
                final SubSessionManager subSessionManager = new SubSessionManager(subSession, this, Paths.get(this.cache, subSession).toString(), this.logger);
                subSessionManager.init();
                subSessionManager.friendManager().initAutoFriend(friendSyncConfig);
                this.subSessionManagers.put(subSession, subSessionManager);
            } catch (final SessionCreationException | SessionUpdateException e) {
                this.logger.error("Failed to create sub-session " + subSession, e);
                // TODO Retry creation after 30s or so
            }
        }
    }

    @Override
    protected boolean handleFriendship() {
        // Don't do anything as we are the main session
        return false;
    }

    /**
     * Update the current session with new information
     *
     * @param sessionInfo The information to update the session with
     * @throws SessionUpdateException If the update failed
     */
    public void updateSession(final SessionInfo sessionInfo) throws SessionUpdateException {
        this.sessionInfo.updateSessionInfo(sessionInfo);
        this.updateSession();
    }

    @Override
    protected void updateSession() throws SessionUpdateException {
        // Make sure the websocket connection is still active
        this.checkConnection();

        final String responseBody = super.updateSessionInternal(Constants.CREATE_SESSION.formatted(this.sessionInfo.getSessionId()), new CreateSessionRequest(this.sessionInfo));
        try {
            final CreateSessionResponse sessionResponse = Constants.OBJECT_MAPPER.readValue(responseBody, CreateSessionResponse.class);

            // Restart if we have 28/30 session members
            final int players = sessionResponse.members().size();
            if (players >= 28) {
                this.logger.info("Restarting session due to " + players + "/30 players");
                this.restart();
            }
        } catch (final JsonProcessingException e) {
            throw new SessionUpdateException("Failed to parse session response: " + e.getMessage());
        }
    }

    /**
     * Stop the current session and close the websocket
     */
    public void shutdown() {
        // Shutdown all sub-sessions
        for (final SubSessionManager subSessionManager : this.subSessionManagers.values()) {
            subSessionManager.shutdown();
        }

        // Shutdown self
        super.shutdown();
        this.scheduledThreadPool.shutdown();
    }

    /**
     * Dump the current and last session responses to json files
     */
    public void dumpSession() {
        this.logger.info("Dumping current and last session responses");
        try {
            final FileWriter file = new FileWriter(this.cache + "/lastSessionResponse.json");
            file.write(this.lastSessionResponse);
            file.close();
        } catch (final IOException e) {
            this.logger.error("Error dumping last session: " + e.getMessage());
        }

        final HttpRequest createSessionRequest = HttpRequest.newBuilder()
                .uri(URI.create(Constants.CREATE_SESSION + this.sessionInfo.getSessionId()))
                .header("Content-Type", "application/json")
                .header("Authorization", this.getTokenHeader())
                .header("x-xbl-contract-version", "107")
                .GET()
                .build();

        try {
            final HttpResponse<String> createSessionResponse = this.httpClient.send(createSessionRequest, HttpResponse.BodyHandlers.ofString());

            final FileWriter file = new FileWriter(this.cache + "/currentSessionResponse.json");
            file.write(createSessionResponse.body());
            file.close();
        } catch (final IOException | InterruptedException e) {
            this.logger.error("Error dumping current session: " + e.getMessage());
        }

        this.logger.info("Dumped session responses to 'lastSessionResponse.json' and 'currentSessionResponse.json'");
    }

    /**
     * Create a sub-session for the given ID
     *
     * @param id The ID of the sub-session to create
     */
    public void addSubSession(final String id) {
        // Make sure we don't already have that ID
        if (this.subSessionManagers.containsKey(id)) {
            this.logger.error("Sub-session already exists with that ID");
            return;
        }

        // Create the sub-session manager
        try {
            final SubSessionManager subSessionManager = new SubSessionManager(id, this, Paths.get(this.cache, id).toString(), this.logger);
            subSessionManager.init();
            subSessionManager.friendManager().initAutoFriend(this.friendSyncConfig);
            this.subSessionManagers.put(id, subSessionManager);
        } catch (final SessionCreationException | SessionUpdateException e) {
            this.logger.error("Failed to create sub-session", e);
            return;
        }

        // Update the list of sub-sessions
        try {
            Files.write(Paths.get(this.cache, "sub_sessions.json"), Constants.OBJECT_MAPPER.writeValueAsBytes(this.subSessionManagers.keySet()));
        } catch (final IOException e) {
            this.logger.error("Failed to update sub-session list", e);
        }
    }

    /**
     * Remove a sub-session for the given ID
     *
     * @param id The ID of the sub-session to remove
     */
    public void removeSubSession(final String id) {
        // Make sure we have that ID
        if (!this.subSessionManagers.containsKey(id)) {
            this.logger.error("Sub-session does not exist with that ID");
            return;
        }

        // Remove the sub-session manager
        this.subSessionManagers.get(id).shutdown();
        this.subSessionManagers.remove(id);

        // Delete the sub-session cache folder and its contents
        try (final Stream<Path> files = Files.walk(Paths.get(this.cache, id))) {
            files.map(Path::toFile)
                    .forEach(File::delete);
            Paths.get(this.cache, id).toFile().delete();
        } catch (final IOException e) {
            this.logger.error("Failed to delete sub-session cache folder", e);
        }

        // Update the list of sub-sessions
        try {
            Files.write(Paths.get(this.cache, "sub_sessions.json"), Constants.OBJECT_MAPPER.writeValueAsBytes(this.subSessionManagers.keySet()));
        } catch (final IOException e) {
            this.logger.error("Failed to update sub-session list", e);
        }

        this.logger.info("Removed sub-session with ID " + id);
    }

    /**
     * List all sessions and their information
     */
    public void listSessions() {
        final List<String> messages = new ArrayList<>();
        this.logger.info("Loading status of sessions...");

        messages.add("Primary Session:");
        messages.add(" - Gamertag: " + this.getXboxToken().gamertag());
        messages.add("   Following: " + this.socialSummary().targetFollowingCount() + "/1000");

        if (!this.subSessionManagers.isEmpty()) {
            messages.add("Sub-sessions:");
            for (final Map.Entry<String, SubSessionManager> subSession : this.subSessionManagers.entrySet()) {
                messages.add(" - ID: " + subSession.getKey());
                messages.add("   Gamertag: " + subSession.getValue().getXboxToken().gamertag());
                messages.add("   Following: " + subSession.getValue().socialSummary().targetFollowingCount() + "/1000");
            }
        } else {
            messages.add("No sub-sessions");
        }

        for (final String message : messages) {
            this.logger.info(message);
        }
    }

    /**
     * Set the callback to run when the session manager needs to be restarted
     *
     * @param restart The callback to run
     */
    public void restartCallback(final Runnable restart) {
        this.restartCallback = restart;
    }

    /**
     * Restart the session manager
     */
    public void restart() {
        if (this.restartCallback != null) {
            this.restartCallback.run();
        } else {
            this.logger.error("No restart callback set");
        }
    }
}
