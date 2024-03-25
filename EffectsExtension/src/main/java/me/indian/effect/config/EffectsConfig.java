package me.indian.effect.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import java.util.List;

public class EffectsConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Komendy które zostaną wykonane gdy gracz odrodzi sie  "})
    @CustomKey("OnSpawn")
    private List<String> onSpawn = List.of("effect <player> resistance 20 100");

    @Comment({""})
    @Comment({"Komendy które zostaną wykonane gdy gracz dołączy na server"})
    @CustomKey("OnJoin")
    private List<String> onJoin = List.of("effect <player> resistance 10 100");

    public List<String> getOnSpawn() {
        return this.onSpawn;
    }

    public List<String> getOnJoin() {
        return this.onJoin;
    }
}