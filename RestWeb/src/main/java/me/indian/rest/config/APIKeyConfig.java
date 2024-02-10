package me.indian.rest.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import java.util.ArrayList;
import java.util.List;

public class APIKeyConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Te klucze api działają dla każdego endpointu"})
    private List<String> powerful = new ArrayList<>();

    @Comment({""})
    @Comment({"Klucze które pozwalają na pobranie backup"})
    private List<String> backup = new ArrayList<>();

    @Comment({""})
    @Comment({"Klucze które pozwalają na wysłanie wiadomości do Discord jeśli integracja jest włączona"})
    private List<String> discord = new ArrayList<>();

    @Comment({""})
    @Comment({"Klucze które pozwalają: wysłać polecenie do konsoli"})
    private List<String> server = new ArrayList<>();

    public List<String> getPowerful() {
        return this.powerful;
    }

    public List<String> getBackup() {
        return this.backup;
    }

    public List<String> getDiscord() {
        return this.discord;
    }

    public List<String> getServer() {
        return this.server;
    }
}
