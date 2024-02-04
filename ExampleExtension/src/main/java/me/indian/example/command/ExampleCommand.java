package me.indian.example.command;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.Command;
import me.indian.bds.server.manager.StatsManager;
import me.indian.bds.util.DateUtil;
import me.indian.example.ExampleExtension;

import java.util.List;

public class ExampleCommand extends Command {

    private final BDSAutoEnable bdsAutoEnable;
    private final StatsManager statsManager;

    public ExampleCommand(final ExampleExtension exampleExtension) {
        super("example", "Przykładowa komenda");
        this.bdsAutoEnable = exampleExtension.getBdsAutoEnable();
        this.statsManager = this.bdsAutoEnable.getServerManager().getStatsManager();


        /**
         *  Tworzymy aliasy aby można było zamiast `!example` użyć np `!exa`
         */
        this.addAlliases(List.of("exa"));

        /**
         *Tworzymy informacje o opcjach i je opisujemy
         * Wyskoczą one gdy w  `onExecute` zwrócimy false
         */
        this.addOption("info", "Informacje o ");
        this.addOption("playtime <nazwa gracza>", "Czas gry danego gracza ");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        final String playerName = this.playerName;

        switch (this.commandSender) {
            case PLAYER -> this.sendMessage("&aPolecenie wykonane przez&3 gracza&b " + playerName);
            //W wypadku konsoli `this.playerName` zwróci nam `CONSOLE`
            case CONSOLE -> this.sendMessage("&aPolecenie wykonane przez&3 konsole&d(&b " + playerName + "&d0");
        }


        if (args.length == 0) {
            this.sendMessage("&aTe polecenie nie zawiera argumentów");
            this.sendMessage("&aJej wykonowaca ma op? :&b " + isOp);
            return true;
        }

        //Tworzymy `!example info`
        if (args[0].equalsIgnoreCase("info")) {
            this.sendMessage("Wywołano&b info");
            return true;
        }

        if (args[0].equalsIgnoreCase("playtime")) {
            if (args.length == 2) {
                final String name = args[1];

                //Z pomocą `StatsManagera` patrzymy ile gracz ma przegrane na serwerze
                //Musimy to jeszcze sformatować
                this.sendMessage(DateUtil.formatTime(this.statsManager.getPlayTimeByName(name), List.of('d', 'h', 'm', 's', 'i')));

            } else {
                this.sendMessage("&cMusisz podać nick gracza");
            }
            return true;
        }

        return false;
    }
}
