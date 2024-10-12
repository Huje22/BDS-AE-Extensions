package pl.indianbartonka.logblock;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "CanBeLocalVariable", "FieldCanBeLocal"})
public class Config extends OkaeriConfig {

    @Comment({""})
    @Comment({"Czy każdy może użyć komendy !logblock?"})
    private boolean logBlockForAll = true;

    @Comment({""})
    @Comment({"Maksymalny rozmiar mapy z danymi"})
    @Comment({"Po przekroczeniu tej wartości mapa zostanie zapisana do pliku"})
    @Comment({"Większa liczba = więcej używanego ramu + więcej danych w aktualnym logblock"})
    @Comment({"Największa liczba jaką możesz tu użyć to: " + Long.MAX_VALUE})
    private long maxBrokenBlockMapSize = 100000;
    private long maxPlacedBlockMapSize = 100000;
    private long maxOpenedContainerMapSize = 100000;
    private long maxInteractedEntityWithContainer = 100000;

    @Comment({""})
    @Comment({"Lista bloków które nie musza zostać pokazane "})
    private List<String> commonBlocks = List.of("minecraft:diorite", "minecraft:granite", "minecraft:andesite",
            "minecraft:cobbled_deepslate", "minecraft:deepslate", "minecraft:grass_block", "minecraft:dirt", "minecraft:netherrack",
            "minecraft:stone", "minecraft:cobblestone", "minecraft:blackstone", "minecraft:basalt"
    );

    public boolean isLogBlockForAll() {
        return this.logBlockForAll;
    }

    public long getMaxBrokenBlockMapSize() {
        return this.maxBrokenBlockMapSize;
    }

    public long getMaxPlacedBlockMapSize() {
        return this.maxPlacedBlockMapSize;
    }

    public long getMaxOpenedContainerMapSize() {
        return this.maxOpenedContainerMapSize;
    }

    public long getMaxInteractedEntityWithContainer() {
        return this.maxInteractedEntityWithContainer;
    }

    public List<String> getCommonBlocks() {
        return this.commonBlocks;
    }
}