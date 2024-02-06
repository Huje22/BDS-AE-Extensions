package me.indian.discord.jda.listener;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.ConsoleColors;
import me.indian.bds.logger.Logger;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.manager.ServerManager;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.discord.DiscordExtension;
import me.indian.discord.config.DiscordConfig;
import me.indian.discord.config.MessagesConfig;
import me.indian.discord.config.sub.LinkingConfig;
import me.indian.discord.embed.component.Footer;
import me.indian.discord.jda.DiscordJDA;
import me.indian.discord.jda.manager.LinkingManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter implements JDAListener {

    private final DiscordJDA discordJDA;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final DiscordConfig discordConfig;
    private final MessagesConfig messagesConfig;
    private TextChannel textChannel;
    private TextChannel consoleChannel;
    private ServerProcess serverProcess;

    public MessageListener(final DiscordJDA DiscordJDA, final DiscordExtension discordExtension) {
        this.discordJDA = DiscordJDA;
        this.bdsAutoEnable = discordExtension.getBdsAutoEnable();
        this.logger = this.bdsAutoEnable.getLogger();
        this.discordConfig = discordExtension.getConfig();
        this.messagesConfig = discordExtension.getMessagesConfig();
    }

    @Override
    public void init() {
        this.textChannel = this.discordJDA.getTextChannel();
        this.consoleChannel = this.discordJDA.getConsoleChannel();
    }

    @Override
    public void initServerProcess(final ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @Override
    public void onMessageUpdate(final MessageUpdateEvent event) {
        if (event.getAuthor().equals(this.discordJDA.getJda().getSelfUser())) return;

        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();

        if (event.getChannel().asTextChannel() == this.textChannel) this.sendMessage(member, author, message, true);
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().equals(this.discordJDA.getJda().getSelfUser())) return;

        final Member member = event.getMember();
        final User author = event.getAuthor();
        final Message message = event.getMessage();
        final String rawMessage = message.getContentRaw();
        final ServerManager serverManager = this.bdsAutoEnable.getServerManager();
        final LinkingConfig linkingConfig = this.discordConfig.getBotConfig().getLinkingConfig();

        if (member == null) return;

        final long id = member.getIdLong();


        if (event.getChannel().asTextChannel() == this.textChannel) {

            /*
             Usuwanie wiadomości jeśli użytkownik ma status offline
             */

            if (this.discordJDA.isCacheFlagEnabled(CacheFlag.ONLINE_STATUS)) {
                final OnlineStatus memberStatus = member.getOnlineStatus();
                if (memberStatus == OnlineStatus.OFFLINE || memberStatus == OnlineStatus.INVISIBLE) {
                    this.discordJDA.mute(member, 10, TimeUnit.SECONDS);
                    message.delete().queue();

                    this.discordJDA.log("Status aktywności offline",
                            "Wiadomość została usunięta z powodu statusu aktywności, jej treść to\n```" +
                                    rawMessage + "```",
                            new Footer(this.discordJDA.getUserName(member, author), member.getEffectiveAvatarUrl()));

                    this.discordJDA.sendPrivateMessage(author, "Nie możesz wysyłać wiadomość na tym kanale " +
                            "gdy twój status aktywności to `" + memberStatus + "`");
                    return;
                }
            }

            /*
              Usuwanie wiadomości jeśli użytkownik nie ma połączonych kont lub jest wyciszony
             */

            final LinkingManager linkingManager = this.discordJDA.getLinkingManager();
            if (linkingManager != null) {
                if (!linkingConfig.isCanType()) {
                    if (!linkingManager.isLinked(id) && !author.isBot()) {
                        this.discordJDA.mute(member, 10, TimeUnit.SECONDS);
                        message.delete().queue();

                        this.discordJDA.log("Brak połączonych kont",
                                "Wiadomość została usunięta z powodu braku połączonych kont, jej treść to:\n```" +
                                        rawMessage + "```",
                                new Footer(this.discordJDA.getUserName(member, author), member.getEffectiveAvatarUrl()));

                        this.discordJDA.sendPrivateMessage(author, linkingConfig.getCantTypeMessage());
                        return;
                    }
                }

                if (serverManager.isMuted(linkingManager.getNameByID(id))) {
                    this.discordJDA.mute(member, 10, TimeUnit.SECONDS);
                    message.delete().queue();
                    this.discordJDA.log("Wyciszenie w Minecraft",
                            "Wiadomość została usunięta z powodu wyciszenia w minecraft, jej treść to\n```" +
                                    rawMessage + "```",
                            new Footer(this.discordJDA.getUserName(member, author), member.getEffectiveAvatarUrl()));
                    this.discordJDA.sendPrivateMessage(author, "Jesteś wyciszony!");
                    return;
                }
            }

            this.sendMessage(member, author, message, false);
        }

        if (event.getChannel().asTextChannel() == this.consoleChannel) {
            if (!this.serverProcess.isEnabled()) return;
            if (member.hasPermission(Permission.ADMINISTRATOR)) {
                this.serverProcess.sendToConsole(rawMessage);
                this.logger.print("[" + DateUtil.getDate() + " DISCORD] " +
                        this.discordJDA.getUserName(member, author) +
                        " (" + author.getIdLong() + ") -> " +
                        rawMessage);
            } else {
                event.getChannel().sendMessage("Nie masz uprawnień administratora aby wysłać tu wiadomość").queue(msg -> {
                    msg.delete().queueAfter(5, TimeUnit.SECONDS);
                    message.delete().queueAfter(4, TimeUnit.SECONDS);
                });
            }
        }
    }

    private void sendMessage(final Member member, final User author, final Message message, final boolean edited) {
        if (!this.messagesConfig.isSendDiscordToMinecraft() || this.isMaxLength(message)) return;

        final Role role = this.discordJDA.getHighestRole(author.getIdLong());
        String msg = this.messagesConfig.getDiscordToMinecraftMessage()
                .replaceAll("<name>", this.discordJDA.getUserName(member, author))
                .replaceAll("<msg>", this.generateRawMessage(message))
                .replaceAll("<reply>", this.generatorReply(message.getReferencedMessage()))
                .replaceAll("<role>", this.discordJDA.getColoredRole(role));

        if (edited) {
            msg += this.messagesConfig.getEdited();
        }
        if (message.isWebhookMessage()) {
            msg += this.messagesConfig.getWebhook();
        }

        msg = MessageUtil.fixMessage(msg);

        if (this.serverProcess.isEnabled()) this.serverProcess.tellrawToAll(msg);
        this.logger.info(msg);
        this.discordJDA.writeConsole(ConsoleColors.removeColors(msg));
    }

    private boolean isMaxLength(final Message message) {
        if (!this.discordConfig.getBotConfig().isDeleteOnReachLimit()) return false;

        if (message.getContentRaw().length() >= this.discordConfig.getBotConfig().getAllowedLength()) {
            this.discordJDA.sendPrivateMessage(message.getAuthor(), this.discordConfig.getBotConfig().getReachedMessage());
            message.delete().queue();
            this.discordJDA.sendPrivateMessage(message.getAuthor(), "`" + message.getContentRaw() + "`");
            return true;
        }
        return false;
    }

    private String generateRawMessage(final Message message) {
        final List<Member> members = message.getMentions().getMembers();
        String rawMessage = MessageUtil.fixMessage(message.getContentRaw());

        if (!message.getAttachments().isEmpty())
            rawMessage += this.messagesConfig.getAttachment();
        if (members.isEmpty()) {
            for (final User user : message.getMentions().getUsers()) {
                if (user != null)
                    rawMessage = rawMessage.replaceAll("<@" + user.getIdLong() + ">", "@" + this.discordJDA.getUserName(null, user));
            }
        } else {
            for (final Member member : members) {
                if (member != null)
                    rawMessage = rawMessage.replaceAll("<@" + member.getIdLong() + ">", "@" + this.discordJDA.getUserName(member, member.getUser()));
            }
        }

        for (final GuildChannel guildChannel : message.getMentions().getChannels()) {
            if (guildChannel != null)
                rawMessage = rawMessage.replaceAll("<#" + guildChannel.getIdLong() + ">", "#" + guildChannel.getName());
        }

        for (final Role role : message.getMentions().getRoles()) {
            if (role != null)
                rawMessage = rawMessage.replaceAll("<@&" + role.getIdLong() + ">", this.discordJDA.getColoredRole(role) + "&r");
        }

        //Daje to aby określić czy wiadomość nadal jest pusta
        if (rawMessage.isEmpty()) rawMessage += message.getJumpUrl();

        return rawMessage;
    }

    private String generatorReply(final Message messageReference) {
        if (messageReference == null) return "";

        final Member member = messageReference.getMember();
        final User author = messageReference.getAuthor();

        final String replyStatement = this.messagesConfig.getReplyStatement()
                .replaceAll("<msg>", this.generateRawMessage(messageReference).replaceAll("\\*\\*", ""))
                .replaceAll("<author>", this.discordJDA.getUserName(member, author));

        if (author.equals(this.discordJDA.getJda().getSelfUser()))
            return this.messagesConfig.getBotReplyStatement()
                    .replaceAll("<msg>", this.generateRawMessage(messageReference)
                            .replaceAll("\\*\\*", ""));


        return replyStatement;
    }
}
