package me.indian.discord.config.sub;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import java.util.ArrayList;
import java.util.List;

public class RestAPIConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Te ustawienia są dostępne tylko wtedy gdy jest dostępne rozserzenie 'RestWebsite'"})

    //TODO: Wygeneruj jakiś klucz
    @Comment({""})
    @Comment({"Klucze które pozwalają na wysłanie wiadomości do Discord jeśli integracja jest włączona"})
    private List<String> discord = new ArrayList<>();


    public List<String> getDiscord() {
        return this.discord;
    }
}
