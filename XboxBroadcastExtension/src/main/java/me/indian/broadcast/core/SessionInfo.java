package me.indian.broadcast.core;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class SessionInfo extends OkaeriConfig {
    @Comment({""})
    @Comment({"Nie bierze nazwy hosta z servera tylko używa tej poniżej"})
    @Comment({""})
    private boolean customHostName = true;
    @Comment({"Nie bierze nazwy świata z servera tylko używa tej poniżej"})
    private boolean customWorldName = true;
    private String hostName = "SkyblockPE";
    private String worldName = "SkyblockPE";
    @Comment({""})
    private String version = "1.20.61";
    private int protocol = 649;
    private int players = 0;
    private int maxPlayers = 20;
    private String ip = "play.skyblockpe.com";
    private int port = 19132;


    public boolean isCustomHostName() {
        return this.customHostName;
    }

    public void setCustomHostName(final boolean customHostName) {
        this.customHostName = customHostName;
    }

    public String getHostName() {
        return this.hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public boolean isCustomWorldName() {
        return this.customWorldName;
    }

    public void setCustomWorldName(final boolean customWorldName) {
        this.customWorldName = customWorldName;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public void setWorldName(final String worldName) {
        this.worldName = worldName;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public int getProtocol() {
        return this.protocol;
    }

    public void setProtocol(final int protocol) {
        this.protocol = protocol;
    }

    public int getPlayers() {
        return this.players;
    }

    public void setPlayers(final int players) {
        this.players = players;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public void setMaxPlayers(final int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(final String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "SessionInfo{" +
                "hostName='" + this.hostName + '\'' +
                ", worldName='" + this.worldName + '\'' +
                ", version='" + this.version + '\'' +
                ", protocol=" + this.protocol +
                ", players=" + this.players +
                ", maxPlayers=" + this.maxPlayers +
                ", ip='" + this.ip + '\'' +
                ", port=" + this.port +
                '}';
    }
}