package pl.indianbartonka.logblock.util;

import pl.indianbartonka.bds.player.position.Position;

public record SerializedBlockEvent(String playerName, String blockID, Position blockPosition) {
}
