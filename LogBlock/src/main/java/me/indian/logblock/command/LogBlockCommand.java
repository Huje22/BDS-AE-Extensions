package me.indian.logblock.command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import me.indian.bds.command.Command;
import me.indian.bds.command.CommandSender;
import me.indian.bds.event.Position;
import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerInteractContainerEvent;
import me.indian.bds.event.player.PlayerInteractEntityWithContainerEvent;
import me.indian.logblock.LogBlockExtension;
import me.indian.logblock.listener.PlayerListener;

public class LogBlockCommand extends Command {

    private final LogBlockExtension extension;
    private final Map<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> blockBreakHistory;
    private final Map<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> blockPlaceHistory;
    private final Map<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> openedContainerHistory;
    private final Map<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> interactedEntityWithContainerHistory;

    public LogBlockCommand(final LogBlockExtension extension, final PlayerListener playerListener) {
        super("logblock", "Informacje na temat interakcji w danym obszarze");
        this.extension = extension;
        this.blockBreakHistory = playerListener.getBlockBreakHistory();
        this.blockPlaceHistory = playerListener.getBlockPlaceHistory();
        this.openedContainerHistory = playerListener.getOpenedContainerHistory();
        this.interactedEntityWithContainerHistory = playerListener.getInteractedEntityWithContainerHistory();

        this.addAlliases(List.of("lb"));
        this.addOption("range <int>" , "Liczba z jaką zostanie przeszukany teren");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.extension.getConfig().isLogBlockForAll() && !isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            return true;
        }

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

        int range = 10;

        if (args.length == 1) {
            try {
                range = Integer.parseInt(args[0]);
            } catch (final NumberFormatException ignored) {
            }
        }

        this.sendMessage("&a----&bZniszczone bloki w zasięgu&1 " + range + "&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> entry : this.blockBreakHistory.entrySet()) {
            if (this.distanceToWithout(entry.getKey(), playerPosition) <= range) {
                anyBroken = true;
                entry.getValue().forEach((dateTime, event) -> {
                    final Position position = event.getBlockPosition();
                    this.sendMessage("&d" + getTime(dateTime) + " &aGracz&b " + event.getPlayerName() + " &eBlok:&1 " + event.getBlockID() + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                });
            }
        }

        if (!anyBroken) {
            this.sendMessage("&aW obszarze&1 " + range + "&a bloków nikt nic nie zniszczył");
        }

        this.sendMessage(" ");

        this.sendMessage("&a----&bPostawione bloki w zasięgu&1 " + range + "&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> entry : this.blockPlaceHistory.entrySet()) {
            if (this.distanceToWithout(entry.getKey(), playerPosition) <= range) {
                anyPlaced = true;
                entry.getValue().forEach((dateTime, event) -> {
                    final Position position = event.getBlockPosition();
                    this.sendMessage("&d" + getTime(dateTime) + " &aGracz&b " + event.getPlayerName() + " &eBlok:&1 " + event.getBlockID() + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                });
            }
        }

        if (!anyPlaced) {
            this.sendMessage("&aW obszarze&1 " + range + "&a bloków nikt nic nie postawił");
        }

        this.sendMessage(" ");

        this.sendMessage("&a----&bOtworzone kontenery w zasięgu&1 " + range + "&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> entry : this.openedContainerHistory.entrySet()) {
            if (this.distanceToWithout(entry.getKey(), playerPosition) <= range) {
                anyOpened = true;
                entry.getValue().forEach((dateTime, event) -> {
                    final Position position = event.getBlockPosition();
                    this.sendMessage("&d" + getTime(dateTime) + " &aGracz&b " + event.getPlayerName() + " &eBlok:&1 " + event.getBlockID() + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                });
            }
        }

        if (!anyOpened) {
            this.sendMessage("&aW obszarze&1 " + range + "&a bloków nikt nic nie otwierał");
        }

        this.sendMessage(" ");

        this.sendMessage("&a----&bInterakcja z mobami posiadającymi kontener w zasięgu&1 " + range + "&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> entry : this.interactedEntityWithContainerHistory.entrySet()) {
            if (this.distanceToWithout(entry.getKey(), playerPosition) <= range) {
                anyEntityInteract = true;
                entry.getValue().forEach((dateTime, event) -> {
                    final Position position = event.getEntityPosition();
                    this.sendMessage("&d" + getTime(dateTime) + " &aGracz&b " + event.getPlayerName() + " &eMob:&1 " + event.getEntityID() + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                });
            }
        }

        if (!anyEntityInteract) {
            this.sendMessage("&aW obszarze&1 " + range + "&a bloków nikt nie wykonał interakcji z mobami posiadającymi kontener");
        }

        this.sendMessage(" ");

        return true;
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
