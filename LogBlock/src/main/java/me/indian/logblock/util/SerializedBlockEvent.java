package me.indian.logblock.util;

import me.indian.bds.player.position.Position;

public record SerializedBlockEvent(String playerName, String blockID, Position blockPosition) {
}
