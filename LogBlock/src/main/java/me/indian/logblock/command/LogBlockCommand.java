package me.indian.logblock.command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import me.indian.bds.command.Command;
import me.indian.bds.event.Position;
import me.indian.bds.event.player.PlayerBlockBreakEvent;
import me.indian.bds.event.player.PlayerBlockPlaceEvent;
import me.indian.bds.event.player.PlayerInteractContainerEvent;
import me.indian.bds.event.player.PlayerInteractEntityWithContainerEvent;
import me.indian.logblock.LogBlockExtension;
import me.indian.logblock.history.HistoryManager;

public class LogBlockCommand extends Command {

    private final LogBlockExtension extension;
    private final HistoryManager historyManager;
    private final Map<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> blockBreakHistory;
    private final Map<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> blockPlaceHistory;
    private final Map<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> openedContainerHistory;
    private final Map<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> interactedEntityWithContainerHistory;

    public LogBlockCommand(final LogBlockExtension extension) {
        super("logblock", "Informacje na temat interakcji w danym obszarze");
        this.extension = extension;
        this.historyManager = extension.getHistoryManager();
        this.blockBreakHistory = this.historyManager.getBrokenBlockHistory().getHistory();
        this.blockPlaceHistory = this.historyManager.getPlacedBlockHistory().getHistory();
        this.openedContainerHistory = this.historyManager.getOpenedContainerHistory().getHistory();
        this.interactedEntityWithContainerHistory = this.historyManager.getInteractedEntityWithContainerHistory().getHistory();

        this.addAlliases(List.of("lb"));
        this.addOption("range <int>", "Liczba z jaką zostanie przeszukany teren");
        this.addOption("commonBlocks <boolean>", "Czy pokazywać bloki które są pospolite?");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!this.extension.getConfig().isLogBlockForAll() && !isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            return true;
        }

        final Position playerPosition = this.getPosition();

        if (this.player == null) {
            this.sendMessage("&4Musisz być graczem!");
            return true;
        }

        if (playerPosition == null) {
            this.sendMessage("&cTwoje pozycje są błędne!");
            return true;
        }

        int range = 10;
        boolean staticCommonBlocks = true;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!isOp) {
                    this.sendMessage("Nie masz odpowiednich uprawnień do wykonania tego polecenia");
                    return true;
                }
                try {
                    this.extension.reloadConfig();
                    this.sendMessage("&aPrzeładowano pliki konfiguracyjne");
                } catch (final Exception exception) {
                    this.extension.getLogger().error("&cNie udało się przeładować configu", exception);
                    this.sendMessage("&cNie udało się przeładować plików konfiguracyjnych");
                }
                return true;
            }

            try {
                range = Integer.parseInt(args[0]);
            } catch (final NumberFormatException ignored) {
            }
        } else if (args.length == 2) {
            try {
                staticCommonBlocks = Boolean.parseBoolean(args[1]);
            } catch (final Exception exception) {
                this.sendMessage("&cDrugi argument przyjmuje tylko&b true&c/&bfalse");
            }
        }

        this.handleBrokenBlocks(range, playerPosition, staticCommonBlocks);
        this.handlePlacedBlocks(range, playerPosition, staticCommonBlocks);
        this.handleOpenedContainers(range, playerPosition, staticCommonBlocks);
        this.handleInteractedEntities(range, playerPosition);

        return true;
    }

    private void handleBrokenBlocks(final int range, final Position playerPosition, final boolean staticCommonBlocks) {
        boolean anyBroken = false;
        this.sendMessage("&a----&bZniszczone bloki w zasięgu&1 " + range + "&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerBlockBreakEvent>> entry : this.blockBreakHistory.entrySet()) {
            if (this.distanceTo(entry.getKey(), playerPosition) <= range) {
                for (final Map.Entry<LocalDateTime, PlayerBlockBreakEvent> entry2 : entry.getValue().entrySet()) {
                    final LocalDateTime dateTime = entry2.getKey();
                    final PlayerBlockBreakEvent event = entry2.getValue();

                    final Position position = event.getBlockPosition();
                    final String blockID = event.getBlockID();

                    if (this.isCommon(blockID, staticCommonBlocks)) {
                        anyBroken = true;
                        this.sendMessage("&d" + this.getTime(dateTime) + " &aGracz&b " + event.getPlayer().getPlayerName() + " &eBlok:&1 " + blockID + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                    }
                }
            }
        }

        if (!anyBroken) {
            this.sendMessage("&aW obszarze&1 " + range + "&a bloków nikt nic nie zniszczył");
        }

        this.sendMessage(" ");
    }

    private void handlePlacedBlocks(final int range, final Position playerPosition, final boolean staticCommonBlocks) {
        boolean anyPlaced = false;
        this.sendMessage("&a----&bPostawione bloki w zasięgu&1 " + range + "&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerBlockPlaceEvent>> entry : this.blockPlaceHistory.entrySet()) {
            if (this.distanceTo(entry.getKey(), playerPosition) <= range) {
                for (final Map.Entry<LocalDateTime, PlayerBlockPlaceEvent> entry2 : entry.getValue().entrySet()) {
                    final LocalDateTime dateTime = entry2.getKey();
                    final PlayerBlockPlaceEvent event = entry2.getValue();

                    final Position position = event.getBlockPosition();
                    final String blockID = event.getBlockID();

                    if (this.isCommon(event.getBlockID(), staticCommonBlocks)) {
                        anyPlaced = true;
                        this.sendMessage("&d" + this.getTime(dateTime) + " &aGracz&b " + event.getPlayer().getPlayerName() + " &eBlok:&1 " + blockID + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                    }
                }
            }
        }

        if (!anyPlaced) {
            this.sendMessage("&aW obszarze&1 " + range + "&a bloków nikt nic nie postawił");
        }

        this.sendMessage(" ");
    }

    private void handleOpenedContainers(final int range, final Position playerPosition, final boolean staticCommonBlocks) {
        boolean anyOpened = false;
        this.sendMessage("&a----&bOtworzone kontenery w zasięgu&1 " + range + "&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerInteractContainerEvent>> entry : this.openedContainerHistory.entrySet()) {
            if (this.distanceTo(entry.getKey(), playerPosition) <= range) {
                for (final Map.Entry<LocalDateTime, PlayerInteractContainerEvent> entry2 : entry.getValue().entrySet()) {
                    final LocalDateTime dateTime = entry2.getKey();
                    final PlayerInteractContainerEvent event = entry2.getValue();

                    final Position position = event.getBlockPosition();
                    final String blockID = event.getBlockID();

                    if (this.isCommon(event.getBlockID(), staticCommonBlocks)) {
                        anyOpened = true;
                        this.sendMessage("&d" + this.getTime(dateTime) + " &aGracz&b " + event.getPlayer().getPlayerName() + " &eBlok:&1 " + blockID + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                    }
                }
            }
        }

        if (!anyOpened) {
            this.sendMessage("&aW obszarze&1 " + range + "&a bloków nikt nic nie otwierał");
        }

        this.sendMessage(" ");
    }

    private void handleInteractedEntities(final int range, final Position playerPosition) {
        boolean anyEntityInteract = false;
        this.sendMessage("&a----&bInterakcja z mobami posiadającymi kontener w zasięgu&1 " + range + "&b kratek&a----");
        for (final Map.Entry<Position, Map<LocalDateTime, PlayerInteractEntityWithContainerEvent>> entry : this.interactedEntityWithContainerHistory.entrySet()) {
            if (this.distanceTo(entry.getKey(), playerPosition) <= range) {
                anyEntityInteract = true;
                entry.getValue().forEach((dateTime, event) -> {
                    final Position position = event.getEntityPosition();
                    this.sendMessage("&d" + this.getTime(dateTime) + " &aGracz&b " + event.getPlayer().getPlayerName() + " &eMob:&1 " + event.getEntityID() + " &cX:" + position.x() + " &aY:" + position.y() + " &9Z:" + position.z());
                });
            }
        }

        if (!anyEntityInteract) {
            this.sendMessage("&aW obszarze&1 " + range + "&a bloków nikt nie wykonał interakcji z mobami posiadającymi kontener");
        }

        this.sendMessage(" ");
    }


    public String getTime(final LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public double distanceTo(final Position mian, final Position other) {
        final double dx = mian.x() - other.x();
        final double dz = mian.z() - other.z();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public boolean isCommon(final String blockID, final boolean skipCommon) {
        if (!skipCommon) return true;
        return !this.extension.getConfig().getCommonBlocks().contains(blockID);
    }
}
