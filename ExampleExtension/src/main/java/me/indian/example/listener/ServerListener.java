package me.indian.example.listener;

import me.indian.bds.event.Listener;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.event.server.ServerStartEvent;
import me.indian.bds.event.server.TPSChangeEvent;

public class ServerListener extends Listener {


    @Override
    public PlayerChatResponse onPlayerChat(final PlayerChatEvent event) {
        final String playerName = event.getPlayerName();
        final String message = event.getMessage();


        //'onPlayerChat' zwraca 'PlayerChatResponse' aby wiedzieć jaki format ma być wiadomości na czacie
        // Gdy są one przez aplikacje 'event.isAppHandled()'
        return new PlayerChatResponse(playerName + " >> " + message);
    }

    @Override
    public void onServerStart(final ServerStartEvent event) {
        System.out.println("Włączono server");
    }

    @Override
    public void onTpsChange(final TPSChangeEvent event) {

    }
}
