package pl.indianbartonka.js.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;

@Header("################################################################")
@Header("#           Ustawienia skryptowania w JavaScript               #")
@Header("################################################################")
public class Config extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy wypisać dostępne silniki JavaScript przy starcje?"})
    private boolean printEngines = true;

    public boolean isPrintEngines() {
        return this.printEngines;
    }
}