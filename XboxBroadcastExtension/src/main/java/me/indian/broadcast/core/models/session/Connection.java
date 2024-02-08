package me.indian.broadcast.core.models.session;

import me.indian.broadcast.core.Constants;

public record Connection(
        int ConnectionType,
        String HostIpAddress,
        int HostPort,
        String RakNetGUID
) {
    public Connection(final String ip, final int port) {
        this(Constants.ConnectionTypeUPNP, ip, port, "");
    }
}
