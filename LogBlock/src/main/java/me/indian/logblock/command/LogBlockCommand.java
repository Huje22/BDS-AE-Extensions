package me.indian.logblock.command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import me.indian.bds.command.Command;
import me.indian.bds.command.CommandSender;
import me.indian.bds.event.Position;
import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerInteractContainerEvent;
import me.indian.bds.event.player.PlayerInteractEntityWithContainerEvent;
import me.indian.logblock.listener.PlayerListener;

public class LogBlockCommand extends Command {

    private final PlayerListener playerListener;
    private final Map<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> blockBreakHistory;
    private final Map<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> blockPlaceHistory;
    private final Map<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> openedContainerHistory;
    private final Map<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> interactedEntityWithContainerHistory;


    public LogBlockCommand(final PlayerListener playerListener) {
        super("lb", "");
        this.playerListener = playerListener;
        this.blockBreakHistory = playerListener.getBlockBreakHistory();
        this.blockPlaceHistory = playerListener.getBlockPlaceHistory();
        this.openedContainerHistory = playerListener.getOpenedContainerHistory();
        this.interactedEntityWithContainerHistory = playerListener.getInteractedEntityWithContainerHistory();

    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        final Position playerPosition = this.getPosition();

        if (this.commandSender == CommandSender.CONSOLE) {
            this.sendMessage("&4Musisz być graczem!");
            return true;
        }

        if (playerPosition == null) {
            this.sendMessage("&cTwoje pozycje są błędne!");
            return true;
        }

        boolean anyBroken = false;
        boolean anyPlaced = false;
        boolean anyOpened = false;
        boolean anyEntityInteract = false;

//TODO: Dystans jako podany int, domyślnie 10
        
        this.sendMessage("&a----&bZniszczone bloki w zasięgu&1 10&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> entry : this.blockBreakHistory.entrySet()) {
            if (this.distanceToWithout(entry.getKey(), playerPosition) <= 10) {
                anyBroken = true;
                entry.getValue().forEach((dateTime, event) -> {
                    final Position position = event.getBlockPosition();
                    this.sendMessage("&d" + getTime(dateTime) + " &aGracz&b " + event.getPlayerName() + " &eBlok:&1 " + event.getBlockID() + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                });
            }
        }

        if (!anyBroken) {
            this.sendMessage("&aW obszarze 10 bloków nikt nic nie zniszczył");
        }

        this.sendMessage(" ");

        this.sendMessage("&a----&bPostawione bloki w zasięgu&1 10&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> entry : this.blockPlaceHistory.entrySet()) {
            if (this.distanceToWithout(entry.getKey(), playerPosition) <= 10) {
                anyPlaced = true;
                entry.getValue().forEach((dateTime, event) -> {
                    final Position position = event.getBlockPosition();
                    this.sendMessage("&d" + getTime(dateTime) + " &aGracz&b " + event.getPlayerName() + " &eBlok:&1 " + event.getBlockID() + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                });
            }
        }

        if (!anyPlaced) {
            this.sendMessage("&aW obszarze 10 bloków nikt nic nie postawił");
        }

        this.sendMessage(" ");

        this.sendMessage("&a----&bOtworzone kontenery w zasięgu&1 10&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> entry : this.openedContainerHistory.entrySet()) {
            if (this.distanceToWithout(entry.getKey(), playerPosition) <= 10) {
                anyOpened = true;
                entry.getValue().forEach((dateTime, event) -> {
                    final Position position = event.getBlockPosition();
                    this.sendMessage("&d" + getTime(dateTime) + " &aGracz&b " + event.getPlayerName() + " &eBlok:&1 " + event.getBlockID() + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                });
            }
        }

        if (!anyOpened) {
            this.sendMessage("&aW obszarze 10 bloków nikt nic nie otwierał");
        }

        this.sendMessage(" ");


        this.sendMessage("&a----&bInterakcja z mobami posiadającymi kontener w zasięgu&1 10&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> entry : this.interactedEntityWithContainerHistory.entrySet()) {
            if (this.distanceToWithout(entry.getKey(), playerPosition) <= 10) {
                anyEntityInteract = true;
                entry.getValue().forEach((dateTime, event) -> {
                    final Position position = event.getBlockPosition();
                    this.sendMessage("&d" + getTime(dateTime) + " &aGracz&b " + event.getPlayerName() + " &eMob:&1 " + event.getEntityID() + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                });
            }
        }

        if (!anyEntityInteract) {
            this.sendMessage("&aW obszarze 10 bloków nikt nie wykonał interakcji z mobami posiadającymi kontener");
        }

        this.sendMessage(" ");

        return false;
    }

    public static String getTime(final LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public double distanceToWithout(final Position mian, final Position other) {
        final double dx = mian.x() - other.x();
        final double dz = mian.z() - other.z();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
