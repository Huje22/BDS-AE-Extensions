package me.indian.logblock.util;

import me.indian.bds.event.Position;

public record SerializedBlockEvent(String playerName, String blockID, Position blockPosition) {
}
