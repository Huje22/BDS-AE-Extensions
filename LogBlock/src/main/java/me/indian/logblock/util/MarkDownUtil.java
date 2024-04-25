package me.indian.logblock.util;

import com.google.gson.Gson;
import me.indian.bds.event.Event;
import me.indian.bds.player.position.Position;
import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerInteractContainerEvent;
import me.indian.bds.event.player.PlayerInteractEntityWithContainerEvent;
import me.indian.bds.util.GsonUtil;

public final class MarkDownUtil {

    private final static Gson GSON = GsonUtil.getGson();

    public static String formatInfo(final String date, final Event event) {
        return """
                # DATE
                                
                ```json
                JSON
                ```
                """.replaceAll("DATE", date).replaceAll("JSON", serializeEvent(event));
    }

    private static String serializeEvent(final Event eventToSerialize) {
        String serializedEvent = "";
        if (eventToSerialize instanceof final PlayerBlockBreakEvent event) {
            serializedEvent = serializeBlockEvent(event.getPlayer().getPlayerName(), event.getBlockID(), event.getBlockPosition());
        } else if (eventToSerialize instanceof final PlayerBlockPlaceEvent event) {
            serializedEvent = serializeBlockEvent(event.getPlayer().getPlayerName(), event.getBlockID(), event.getBlockPosition());
        } else if (eventToSerialize instanceof final PlayerInteractContainerEvent event) {
            serializedEvent = serializeBlockEvent(event.getPlayer().getPlayerName(), event.getBlockID(), event.getBlockPosition());
        } else if (eventToSerialize instanceof final PlayerInteractEntityWithContainerEvent event) {
            serializedEvent = serializeEntityEvent(event.getPlayer().getPlayerName(), event.getEntityID(), event.getEntityPosition());
        }

        return serializedEvent;
    }

    private static String serializeEntityEvent(final String playerName, final String entityID, final Position entityPosition) {
        return GSON.toJson(new SerializedEntityEvent(playerName, entityID, entityPosition));
    }

    private static String serializeBlockEvent(final String playerName, final String blockID, final Position blockPosition) {
        return GSON.toJson(new SerializedBlockEvent(playerName, blockID, blockPosition));
    }
}