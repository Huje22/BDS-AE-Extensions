package pl.indianbartonka.example.command;

import java.util.List;
import pl.indianbartonka.bds.BDSAutoEnable;
import pl.indianbartonka.bds.command.Command;
import pl.indianbartonka.bds.server.stats.StatsManager;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.example.ExampleExtension;

public class ExampleCommand extends Command {

    private final StatsManager statsManager;

    public ExampleCommand(final ExampleExtension exampleExtension) {
        super("example", "Przykładowa komenda");
        final BDSAutoEnable bdsAutoEnable = exampleExtension.getBdsAutoEnable();
        this.statsManager = bdsAutoEnable.getServerManager().getStatsManager();


        /*
           Tworzymy aliasy aby można było zamiast `!example` użyć np `!exa`
         */
        this.addAlliases(List.of("exa"));

        /*
         Tworzymy informacje o opcjach i je opisujemy
          Wyskoczą one gdy w  `onExecute` zwrócimy false
         */
        this.addOption("help", "Info o poleceniu ");
        this.addOption("info", "Informacje o ");
        this.addOption("playtime <nazwa gracza>", "Czas gry danego gracza ");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        if (this.player != null) {
            this.sendMessage("&aPolecenie wykonane przez&3 gracza&b " + this.player.getPlayerName());
        } else {
            this.sendMessage("&aPolecenie wykonane przez&3 konsole");
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

        if (args[0].equalsIgnoreCase("help")) {
            //Ta metoda buduje nam pomoc na podstawie dodanych opcji przez 'addOption()'
            this.buildHelp();
            return true;
        }

        if (args[0].equalsIgnoreCase("playtime")) {
            if (args.length == 2) {
                final String name = args[1];

                //Z pomocą `StatsManagera` patrzymy ile gracz ma czasu przegranego na serwerze w mili sekundach
                //Musimy to jeszcze sformatować
                this.sendMessage(DateUtil.formatTimeDynamic(this.statsManager.getPlayTime(name)));

            } else {
                this.sendMessage("&cMusisz podać nick gracza");
            }
            return true;
        }

        return false;
    }
}
