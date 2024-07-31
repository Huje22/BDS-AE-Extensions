package me.indian.broadcast.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import java.util.List;
import me.indian.broadcast.core.SessionInfo;

public class SessionConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"UWAGA: Sesja aktualizuje się tylko wtedy gdy server jest włączony"})
    @Comment("Ilość czasu w sekundach na aktualizację informacji o sesji")
    @Comment("Ostrzeżenie: Ta wartość nie może być mniejsza niż 20 ze względu na limity prędkości Xboxa")
    private int updateInterval = 40;

    @Comment({""})
    @Comment("Czy powinniśmy zapytać serwer Bedrock, aby zsynchronizować informacje o sesji")
    @Comment("Inaczej weźmiemy informacje z serwera zarzadzanego przez aplikacje")
    private boolean queryServer = true;

    @Comment({""})
    @Comment({"Nie bierze nazwy świata z servera tylko używa tej poniżej"})
    private boolean customWorldName = true;

    @Comment({""})
    @Comment({"Nie bierze nazwy hosta z servera tylko używa tej poniżej"})
    private boolean customHostName = true;

    @Comment({""})
    @Comment({"Czy nazwa świata ma być randomowa?"})
    private boolean randomWorldName = true;

    @Comment({""})
    @Comment({"Czy nazwa hosta ma być randomowa?"})
    private boolean randomHostName = true;

    @Comment({""})
    @Comment({"Randomowe nazwy świata"})
    private List<String> randomWorldNames = List.of("Mój super świat!", "Koko Jumbo");

    @Comment({""})
    @Comment({"Randomowe nazwy hosta"})
    private List<String> randomHostNames = List.of("MysteryDuck2188", "Huje22");

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

    public boolean isCustomHostName() {
        return this.customHostName;
    }

    public boolean isCustomWorldName() {
        return this.customWorldName;
    }

    public boolean isRandomWorldName() {
        return this.randomWorldName;
    }

    public boolean isRandomHostName() {
        return this.randomHostName;
    }

    public List<String> getRandomWorldNames() {
        return this.randomWorldNames;
    }

    public List<String> getRandomHostNames() {
        return this.randomHostNames;
    }
}