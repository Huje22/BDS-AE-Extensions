package pl.indianbartonka.example.config.sub;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;

public class SubConfig extends OkaeriConfig {

    @Comment({"A to pyk komentarz2"})
    @CustomKey(("COŚ2"))
    private String cos = "issk";

    public String getCos() {
        return this.cos;
    }
}
