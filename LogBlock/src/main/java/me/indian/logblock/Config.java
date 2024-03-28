package me.indian.logblock;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class Config extends OkaeriConfig {

    @Comment({"Maksymalny rozmiar mapy z danymi"})
    @Comment({"Po przekroczeniu tej wartości mapa zostanie zapisana do pliku"})
    private int maxMapSize = 1000;

    public int getMaxMapSize() {
        return this.maxMapSize;
    }
}
