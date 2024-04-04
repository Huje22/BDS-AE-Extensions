package me.indian.logblock.util;

import me.indian.bds.event.Position;

public record SerializedEntityEvent(String playerName, String entityID, Position entityPosition) {
}
