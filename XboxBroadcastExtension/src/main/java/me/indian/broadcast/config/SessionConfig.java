package me.indian.broadcast.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import me.indian.broadcast.core.SessionInfo;

public class SessionConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"UWAGA: Sesia aktualizuje się tylko wtedy gdy server jest włączony"})
    @Comment("Ilość czasu w sekundach na aktualizację informacji o sesji")
    @Comment("Ostrzeżenie: Ta wartość nie może być mniejsza niż 20 ze względu na limity prędkości Xboxa")
    private int updateInterval = 30;

    @Comment({""})
    @Comment("Czy powinniśmy zapytać serwer Bedrock, aby zsynchronizować informacje o sesji")
    @Comment("Inaczej weźmiemy informacje z serwera zarzadzanego przez aplikacje")
    private boolean queryServer = true;

    @Comment({""})
    @Comment("Dane do rozgłaszania w usłudze Xbox Live, to jest domyślne, jeśli zapytania są włączone")
    private SessionInfo sessionInfo = new SessionInfo();

    public int getUpdateInterval() {
        return this.updateInterval;
    }

    public boolean isQueryServer() {
        return this.queryServer;
    }

    public SessionInfo getSessionInfo() {
        return this.sessionInfo;
    }
}