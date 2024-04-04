package me.indian.example.listener;

import me.indian.bds.event.Listener;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.TPSChangeEvent;
import me.indian.example.ExampleExtension;

public class ServerListener extends Listener {

    private final ExampleExtension extension;

    public ServerListener(final ExampleExtension extension) {
        this.extension = extension;
    }

    @Override
    public PlayerChatResponse onPlayerChat(final PlayerChatEvent event) {
        final String playerName = event.getPlayer().getPlayerName();
        final String message = event.getMessage();


        //'onPlayerChat' zwraca 'PlayerChatResponse' aby wiedzieć jaki format ma być wiadomości na czacie, i czy anulować wysyłanie wiadomości
        // Gdy są one przez aplikacje 'event.isAppHandled()'

        return new PlayerChatResponse(playerName + " >> " + message, false);
    }

    @Override
    public void onServerStart(final ServerStartEvent event) {
        this.extension.getLogger().info("Włączono server");
    }

    @Override
    public void onTpsChange(final TPSChangeEvent event) {
        //Wykonuje akcje gdy TPS ulegną wypisaniu w konsoli
    }
}
