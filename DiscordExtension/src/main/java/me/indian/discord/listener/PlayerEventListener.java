package me.indian.discord.listener;

import java.util.HashMap;
import java.util.Map;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.Listener;
import me.indian.bds.event.player.PlayerChatEvent;
import me.indian.bds.event.player.PlayerDeathEvent;
import me.indian.bds.event.player.PlayerJoinEvent;
import me.indian.bds.event.player.PlayerQuitEvent;
import me.indian.bds.event.player.response.PlayerChatResponse;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MessageUtil;
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
    private final ServerProcess serverProcess;
    private final MessagesConfig messagesConfig;
    private final DiscordJDA discordJDA;
    private final LinkingManager linkingManager;
    private final LinkingConfig linkingConfig;
    private final Map<String, String> cachedPrefixes;

    public PlayerEventListener(final DiscordExtension discordExtension) {
        this.bdsAutoEnable = discordExtension.getBdsAutoEnable();
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
        this.messagesConfig = discordExtension.getMessagesConfig();
        this.discordJDA = discordExtension.getDiscordJDA();
        this.linkingManager = this.discordJDA.getLinkingManager();
        this.linkingConfig = discordExtension.getConfig().getBotConfig().getLinkingConfig();
        this.cachedPrefixes = new HashMap<>();
    }

    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final String playerName = event.getPlayerName();
        this.discordJDA.sendJoinMessage(playerName);

        if (this.linkingManager.isLinked(playerName)) {
            final Member member = this.linkingManager.getMember(playerName);
            if (member != null) {
                final String prefix = this.getRole(member);
                this.setPlayerPrefix(playerName, prefix);
            }
        }
    }

    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final String playerName = event.getPlayerName();
        this.discordJDA.sendLeaveMessage(playerName);
        this.cachedPrefixes.remove(playerName);
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
                this.setPlayerPrefix(playerName, role);
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
        this.discordJDA.sendDeathMessage(event.getPlayerName(), event.getDeathMessage(),
                event.getKillerName(), event.getUsedItemName());
    }

    private void setPlayerPrefix(final String playerName, final String prefix) {
        if(!this.messagesConfig.isShowInName()) return;
        final String cachedPrefix = this.cachedPrefixes.get(playerName);

        if (cachedPrefix != null) {
            if (!cachedPrefix.equals(prefix)) {
                this.serverProcess.sendToConsole("scriptevent bds:tag_prefix " + playerName + " " + MessageUtil.colorize(prefix) + " ");
                this.cachedPrefixes.put(playerName, prefix);
            }
        } else {
            this.serverProcess.sendToConsole("scriptevent bds:tag_prefix " + playerName + " " + MessageUtil.colorize(prefix) + " ");
            this.cachedPrefixes.put(playerName, prefix);
        }

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
