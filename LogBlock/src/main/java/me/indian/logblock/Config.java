package me.indian.logblock;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

@SuppressWarnings({"FieldMayBeFinal", "CanBeLocalVariable"})
public class Config extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy każdy może użyć komendy !logblock?"})
    private boolean logBlockForAll = true;

    @Comment({""})
    @Comment({"Maksymalny rozmiar mapy z danymi"})
    @Comment({"Po przekroczeniu tej wartości mapa zostanie zapisana do pliku"})
    @Comment({"UWAGA Żadna z tych ilości nie była testowana :)"})
    private int  maxBrokenBlockMapSize = 10000;
    private int maxPlacedBlockMapSize = 10000;
    private int maxOpenedContainerMapSize = 10000;
    private int maxInteractedEntityWithContainer = 10000;

    public boolean isLogBlockForAll() {
        return this.logBlockForAll;
    }

    public int getMaxBrokenBlockMapSize() {
        return this.maxBrokenBlockMapSize;
    }

    public int getMaxPlacedBlockMapSize() {
        return this.maxPlacedBlockMapSize;
    }

    public int getMaxOpenedContainerMapSize() {
        return this.maxOpenedContainerMapSize;
    }

    public int getMaxInteractedEntityWithContainer() {
        return this.maxInteractedEntityWithContainer;
    }
}