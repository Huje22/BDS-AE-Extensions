package pl.indianbartonka.example.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import pl.indianbartonka.example.config.sub.SubConfig;

public class Config extends OkaeriConfig {

    @Comment({"A to pyk komentarz 1"})
    @CustomKey(("COŚ1"))
    private boolean cos = true;

    private SubConfig subConfig = new SubConfig();

    public boolean isCos() {
        return this.cos;
    }

    public SubConfig getSubConfig() {
        return this.subConfig;
    }
}
