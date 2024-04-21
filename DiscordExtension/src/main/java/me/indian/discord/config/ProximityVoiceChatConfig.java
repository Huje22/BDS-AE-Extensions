package me.indian.discord.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;

@Header("################################################################")
@Header("#           Ustawienia Czatu głosowego zbliżeniowego           #")
@Header("################################################################")
public class ProximityVoiceChatConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"ID Kategorii voice chatu"})
    @CustomKey("CategoryID")
    private long categoryID = 1L;

    @Comment({""})
    @Comment({"ID Kanału lobby"})
    @CustomKey("LobbyID")
    private long lobbyID = 1L;

    @Comment({""})
    @Comment({"Dozwolona odległość graczy od siebie"})
    @CustomKey("ProximityThreshold")
    private int proximityThreshold = 30;

    @Comment({""})
    @Comment({"Czas odświeżania w sekundach"})
    @CustomKey("RefreshTime")
    private int refreshTime = 1;

    @Comment({""})
    @Comment({"Czy użytkownicy mogą odzywać się na lobby?"})
    @CustomKey("SpeakInLobby")
    private boolean speakInLobby = true;

    public long getCategoryID() {
        return this.categoryID;
    }

    public long getLobbyID() {
        return this.lobbyID;
    }

    public int getProximityThreshold() {
        return this.proximityThreshold;
    }

    public int getRefreshTime() {
        return this.refreshTime;
    }

    public boolean isSpeakInLobby() {
        return this.speakInLobby;
    }
}
