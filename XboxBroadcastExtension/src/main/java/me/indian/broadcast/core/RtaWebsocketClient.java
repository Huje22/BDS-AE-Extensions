package me.indian.broadcast.core;


import me.indian.bds.logger.Logger;
import me.indian.bds.util.GsonUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.util.Map;

/**
 * Handle the connection and authentication with the RTA websocket
 */
public class RtaWebsocketClient extends WebSocketClient {
    private String connectionId;
    private final Logger logger;
    private boolean firstConnectionId = true;

    /**
     * Create a new websocket and add the Authorization header
     *
     * @param authenticationToken The token to use for authentication
     */
    public RtaWebsocketClient(final String authenticationToken, final Logger logger) {
        super(Constants.RTA_WEBSOCKET);
        this.addHeader("Authorization", authenticationToken);
        this.logger = logger;
    }

    /**
     * A helper method to get the stored connection ID
     *
     * @return The stored connection ID
     */
    public String getConnectionId() {
        return this.connectionId;
    }

    /**
     * When the web socket connects send the request for the connection ID
     *
     * @param serverHandshake The handshake of the websocket instance
     * @see WebSocketClient#onOpen(ServerHandshake)
     */
    @Override
    public void onOpen(final ServerHandshake serverHandshake) {
        this.send("[1,1,\"https://sessiondirectory.xboxlive.com/connections/\"]");
    }

    /**
     * When we get a message check if it's a connection ID message
     * and handle otherwise ignore it
     *
     * @param message The UTF-8 decoded message that was received.
     * @see WebSocketClient#onMessage(String)
     */
    @Override
    public void onMessage(final String message) {
        if (message.contains("ConnectionId") && this.firstConnectionId) {
            try {
                final Object[] parts = GsonUtil.getGson().fromJson(message, Object[].class);
                this.connectionId = ((Map<String, String>) parts[4]).get("ConnectionId");
                this.firstConnectionId = false;
            } catch (final Exception ignored) {
            }
        } else {
            this.logger.debug("Wiadomość WebSocket: " + message);
        }
    }

    /**
     * @see WebSocketClient#onClose(int, String, boolean)
     */
    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        this.logger.debug("Websocket odłączone: " + reason + " (" + code + ")");
    }

    /**
     * @see WebSocketClient#onError(Exception)
     **/
    @Override
    public void onError(final Exception ex) {
        this.logger.debug("Websocket error: " + ex.getMessage());
    }
}
