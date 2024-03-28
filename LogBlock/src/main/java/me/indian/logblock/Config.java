package me.indian.logblock;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class Config extends OkaeriConfig {

    @Comment({"Maksymalny rozmiar mapy z danymi"})
    @Comment({"Po przekroczeniu tej wartości mapa zostanie zapisana do pliku"})
    private int maxMapSize = 1000;

//TODO:Dodaj maxMpaSize na pojedyncza mape , zapisuj pliki jako .json, dodaj opcje max zasiegu dla kazdej kategori
    
    public int getMaxMapSize() {
        return this.maxMapSize;
    }
}
