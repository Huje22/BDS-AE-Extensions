package me.indian.discord.listener;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.Listener;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.PlayerDeathEvent;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerQuitEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.util.DateUtil;
import me.indian.discord.DiscordExtension;
import me.indian.discord.config.MessagesConfig;
import me.indian.discord.config.sub.LinkingConfig;
import me.indian.discord.embed.component.Footer;
import me.indian.discord.jda.DiscordJDA;
import me.indian.discord.jda.manager.LinkingManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class PlayerEventListener extends Listener {

    private final BDSAutoEnable bdsAutoEnable;
    private final MessagesConfig messagesConfig;
    private final DiscordJDA discordJDA;
    private final LinkingManager linkingManager;
    private final LinkingConfig linkingConfig;

    public PlayerEventListener(final DiscordExtension discordExtension) {
        this.bdsAutoEnable = discordExtension.getBdsAutoEnable();
        this.messagesConfig = discordExtension.getMessagesConfig();
        this.discordJDA = discordExtension.getDiscordJDA();
        this.linkingManager = this.discordJDA.getLinkingManager();
        this.linkingConfig = discordExtension.getConfig().getBotConfig().getLinkingConfig();
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
        final boolean appHandled = event.isAppHandled();

        if (event.isMuted() && appHandled) {
            this.discordJDA.log("Wyciszenie w Minecraft",
                    "Wiadomość została nie wysłana z powodu wyciszenia w Minecraft, jej treść to:\n```" +
                            message + "```",
                    new Footer(playerName + " " + DateUtil.getTimeHMS()));
            return null;
        }

        boolean memberMutedOnDiscord = false;
        String role = "";

        if (!event.isMuted() && this.messagesConfig.isFormatChat() && this.linkingManager.isLinked(playerName)) {
            final Member member = this.linkingManager.getMember(playerName);
            if (member != null) {
                memberMutedOnDiscord = member.isTimedOut();
                role = this.getRole(member);
            }
        }

        final String format = this.messagesConfig.getChatMessageFormat()
                .replaceAll("<player>", playerName)
                .replaceAll("<message>", event.getMessage())
                .replaceAll("<role>", role.trim());


        if (appHandled) {
            if (!event.isMuted() && !memberMutedOnDiscord) {
                this.discordJDA.sendPlayerMessage(playerName, message);
            }
            if (memberMutedOnDiscord) {
                this.bdsAutoEnable.getServerProcess().tellrawToPlayer(playerName, "&cZostałeś wyciszony na discord!");
                this.discordJDA.log("Wyciszenie na Discord",
                        "Wiadomość została usunięta z powodu wyciszenia na Discord, jej treść to:\n```" +
                                message + "```",
                        new Footer(playerName + " " + DateUtil.getTimeHMS()));
            }

            if (!event.isMuted() && this.messagesConfig.isFormatChat()) {
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

    private String getRole(final Member member) {
        final Role highestRole = this.discordJDA.getHighestRole(member.getIdLong());
        if (highestRole != null) {
            if (this.linkingConfig.isUseCustomRoles()) {
                final long highestAllowedID = this.getHighestFromAllowed(member);

                if (!this.linkingConfig.isOnlyCustomRoles()) {
                    if (highestAllowedID != highestRole.getIdLong()) {
                        return this.discordJDA.getColoredRole(highestRole);
                    }
                }

                final String roleIcon = this.getRoleIcon(highestAllowedID);
                return roleIcon == null ? "" : roleIcon;
            }
        }
        return "";
    }

    private long getHighestFromAllowed(final Member member) {
        for (final Role role : member.getRoles()) {
            if (this.linkingConfig.getCustomRoles().containsKey(role.getIdLong())) {
                return role.getIdLong();
            }
        }
        return -1;
    }

    private String getRoleIcon(final long roleID) {
        return this.linkingConfig.getCustomRoles().get(roleID);
    }
}