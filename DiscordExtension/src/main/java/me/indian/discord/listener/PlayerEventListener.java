package me.indian.discord.listener;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.Listener;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.PlayerDeathEvent;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerQuitEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.discord.DiscordExtension;
import me.indian.discord.config.DiscordConfig;
import me.indian.discord.jda.DiscordJDA;
import me.indian.discord.jda.manager.LinkingManager;
import net.dv8tion.jda.api.entities.Member;

public class PlayerEventListener extends Listener {

    private final DiscordExtension discordExtension;
    private final BDSAutoEnable bdsAutoEnable;
    private final DiscordConfig discordConfig;
    private final DiscordJDA discordJDA;
    private final LinkingManager linkingManager;

    public PlayerEventListener(final DiscordExtension discordExtension) {
        this.discordExtension = discordExtension;
        this.bdsAutoEnable = this.discordExtension.getBdsAutoEnable();
        this.discordConfig = this.discordExtension.getConfig();
        this.discordJDA = this.discordExtension.getDiscordJDA();
        this.linkingManager = this.discordJDA.getLinkingManager();
    }


    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.discordJDA.sendJoinMessage(event.getPlayerName());
    }

//    @Override
//    public void onPlayerSpawn(final PlayerSpawnEvent event) {
//
//    }

    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.discordJDA.sendLeaveMessage(event.getPlayerName());
    }

    @Override
    public PlayerChatResponse onPlayerChat(final PlayerChatEvent event) {
        final String playerName = event.getPlayerName();
        final String message = event.getMessage();

        boolean appHandled = event.isAppHandled();
        boolean memberMutedOnDiscord = false;
        String role = "";

        if (!event.isMuted() && this.discordConfig.getMessagesOptionsConfig().isFormatChat() && this.linkingManager.isLinked(playerName)) {
            final Member member = this.linkingManager.getMember(playerName);
            if (member != null) {
                role = this.discordJDA.getColoredRole(this.discordJDA.getHighestRole(member.getIdLong()));
                memberMutedOnDiscord = member.isTimedOut();
            }
        }

        final String format = this.discordConfig.getMessagesConfig().getChatMessageFormat()
                .replaceAll("<player>", playerName)
                .replaceAll("<message>", event.getMessage())
                .replaceAll("<role>", role);


        if (appHandled) {
            if (!event.isMuted() && !memberMutedOnDiscord) {
                this.discordJDA.sendPlayerMessage(playerName, message);
            }
            if (memberMutedOnDiscord) {
                this.bdsAutoEnable.getServerProcess().tellrawToPlayer(playerName, "&cZostałeś wyciszony na discord!");
            }

            if (!event.isMuted() && this.discordConfig.getMessagesOptionsConfig().isFormatChat()) {
                return new PlayerChatResponse(format, memberMutedOnDiscord);
            }
        } else {
            this.discordJDA.sendPlayerMessage(playerName, message);
        }

        return null;
    }

    @Override
    public void onPlayerDeath(final PlayerDeathEvent event) {
        this.discordJDA.sendDeathMessage(event.getPlayerName(), event.getDeathMessage()
                .replaceAll("§l", "**")
                .replaceAll("§r", "")
        );
    }
}
