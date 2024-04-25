package me.indian.logblock.util;

import me.indian.bds.player.position.Position;

public record SerializedEntityEvent(String playerName, String entityID, Position entityPosition) {
}
