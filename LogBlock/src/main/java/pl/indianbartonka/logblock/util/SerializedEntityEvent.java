package pl.indianbartonka.logblock.util;

import pl.indianbartonka.bds.player.position.Position;

public record SerializedEntityEvent(String playerName, String entityID, Position entityPosition) {
}
