package me.indian.broadcast.command;

import me.indian.bds.command.Command;
import me.indian.broadcast.XboxBroadcastExtension;
import me.indian.broadcast.core.SessionManager;

public class XboxBroadcastCommand extends Command {

    private final XboxBroadcastExtension xboxBroadcastExtension;
    private final SessionManager sessionManager;

    public XboxBroadcastCommand(final XboxBroadcastExtension xboxBroadcastExtension) {
        super("xbox", "Zarządzanie Rozszerzeniem");
        this.xboxBroadcastExtension = xboxBroadcastExtension;
        this.sessionManager = this.xboxBroadcastExtension.getSessionManager();

        this.addOption("reload", "Przeładowuje konfiguracje");
        this.addOption("restart", "Ponowne uruchomienie połączenia z Xbox Live.");
        this.addOption("account list", "Lista kont");
        this.addOption("account <add/remove> <sub-session-id>", "Dodaj/Usuń konto");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (!isOp) {
            this.sendMessage("&cPotrzebujesz wyższych uprawnień");
            this.deniedSound();
            return true;
        }

        if (args.length == 0) {
            this.buildHelp();
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            try {
                this.xboxBroadcastExtension.reloadConfig();
                this.sendMessage("&aPrzeładowano pliki konfiguracyjne");
            } catch (final Exception exception) {
                this.xboxBroadcastExtension.getLogger().error("&cNie udało się przeładować configu", exception);
                this.sendMessage("&cNie udało się przeładować plików konfiguracyjnych");
            }
            return true;
        }

        if (this.player != null) {
            this.sendMessage("&cTo polecenie można uruchomić tylko z konsoli.");
            return true;
        }

        if (args[0].equalsIgnoreCase("restart")) {
            this.xboxBroadcastExtension.restart();
            return true;
        }

        if (args[0].equalsIgnoreCase("account")) {
            if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
                this.sessionManager.listSessions();
                return true;
            }

            if (args.length >= 3) {
                switch (args[1].toLowerCase()) {
                    case "add" -> this.sessionManager.addSubSession(args[2]);
                    case "remove" -> this.sessionManager.removeSubSession(args[2]);
                    default -> {
                        return true;
                    }
                }
                return true;
            }

            return false;
        }


        return false;
    }
}