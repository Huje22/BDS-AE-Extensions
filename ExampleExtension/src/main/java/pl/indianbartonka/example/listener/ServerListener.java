package pl.indianbartonka.example.listener;

import pl.indianbartonka.bds.event.EventHandler;
import pl.indianbartonka.bds.event.Listener;
import pl.indianbartonka.bds.event.player.PlayerChatEvent;
import pl.indianbartonka.bds.event.player.response.PlayerChatResponse;
import pl.indianbartonka.bds.event.server.ServerConsoleCommandEvent;
import pl.indianbartonka.bds.event.server.ServerStartEvent;
import pl.indianbartonka.bds.event.server.TPSChangeEvent;
import pl.indianbartonka.bds.event.server.response.ServerConsoleCommandResponse;
import pl.indianbartonka.example.ExampleExtension;

public class ServerListener implements Listener {

    private final ExampleExtension extension;

    public ServerListener(final ExampleExtension extension) {
        this.extension = extension;
    }

    /**
     * Eventy 'Event' są wywoływane za pomocą refleksji dzięki czemu wystarczy użyć adnotacji '@EventHandler'
     */

    @EventHandler
    private void onServerStart(final ServerStartEvent event) {
        this.extension.getLogger().info("Włączono server");
    }

    @EventHandler
    private void onTpsChange(final TPSChangeEvent event) {
        //Wykonuje akcje gdy TPS ulegną wypisaniu w konsoli
    }

    @EventHandler
    private PlayerChatResponse onPlayerChat(final PlayerChatEvent event) {
        final String playerName = event.getPlayer().getPlayerName();
        final String message = event.getMessage();


        //'onPlayerChat' zwraca 'PlayerChatResponse' aby wiedzieć jaki format ma być wiadomości na czacie, i czy anulować wysyłanie wiadomości
        // Gdy są one przez aplikacje 'event.isAppHandled()'

        return new PlayerChatResponse(playerName + " >> " + message, false);
    }

    @EventHandler
    private ServerConsoleCommandResponse onServerCommand(final ServerConsoleCommandEvent event) {
        return new ServerConsoleCommandResponse(() -> this.extension.getLogger().alert("Wykonano polecenie: " + event.getCommand()));
    }
}
