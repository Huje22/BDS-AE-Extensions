package me.indian.discord.jda;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.logger.ConsoleColors;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.watchdog.module.PackModule;
import me.indian.discord.DiscordExtension;
import me.indian.discord.config.DiscordConfig;
import me.indian.discord.config.sub.BotConfig;
import me.indian.discord.embed.component.Field;
import me.indian.discord.embed.component.Footer;
import me.indian.discord.jda.listener.CommandListener;
import me.indian.discord.jda.listener.JDAListener;
import me.indian.discord.jda.listener.MentionPatternCacheListener;
import me.indian.discord.jda.listener.MessageListener;
import me.indian.discord.jda.manager.LinkingManager;
import me.indian.discord.jda.manager.StatsChannelsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class DiscordJDA {

    private final DiscordExtension discordExtension;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final DiscordConfig discordConfig;
    private final BotConfig botConfig;
    private final long serverID, channelID, consoleID, logID;
    private final List<JDAListener> listeners;
    private final Map<String, Pattern> mentionPatternCache;
    private JDA jda;
    private Guild guild;
    private TextChannel textChannel, consoleChannel, logChannel;
    private StatsChannelsManager statsChannelsManager;
    private LinkingManager linkingManager;

    public DiscordJDA( final DiscordExtension discordExtension) {
        this.discordExtension = discordExtension;
        this.bdsAutoEnable = this.discordExtension.getBdsAutoEnable();
        this.logger = this.bdsAutoEnable.getLogger();
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.discordConfig = this.discordExtension.getConfig();
        this.botConfig = this.discordConfig.getBotConfig();
        this.serverID = this.botConfig.getServerID();
        this.channelID = this.botConfig.getChannelID();
        this.consoleID = this.botConfig.getConsoleID();
        this.logID = this.botConfig.getLogID();
        this.listeners = new ArrayList<>();
        this.mentionPatternCache = new HashMap<>();


        this.listeners.add(new CommandListener(this, this.discordExtension));
        this.listeners.add(new MessageListener(this, this.discordExtension));
        this.listeners.add(new MentionPatternCacheListener(this, this.mentionPatternCache));

    }

    public void init() {
        if (this.botConfig.isEnable()) {
            this.logger.info("&aŁadowanie bota....");
            if (this.botConfig.getToken().isEmpty()) {
                this.logger.alert("&aNie znaleziono tokenu , pomijanie ładowania.");
                this.discordExtension.setBotEnabled(false);
                return;
            }

            final PackModule packModule = this.bdsAutoEnable.getWatchDog().getPackModule();
            if (!packModule.isLoaded()) {
                this.logger.error("&cNie załadowano paczki&b " + packModule.getPackName() + "&4.&cBot nie może bez niej normalnie działać");
                this.logger.error("Możesz znaleźć ją tu:&b https://github.com/Huje22/BDS-Auto-Enable-Managment-Pack");
                return;
            }

            try {
                this.jda = JDABuilder.create(this.botConfig.getToken(), this.getGatewayIntents())
                        .disableCache(this.botConfig.getDisableCacheFlag())
                        .enableCache(this.botConfig.getEnableCacheFlag())
                        .setEnableShutdownHook(false)
                        .build();
                this.jda.awaitReady();
                this.logger.info("&aZaładowano bota");
            } catch (final Exception exception) {
                this.logger.error("&cNie można uruchomić bota", exception);
                this.discordExtension.setBotEnabled(false);
                return;
            }

            this.guild = this.jda.getGuildById(this.serverID);
            if (this.guild == null) {
                this.jda.shutdown();
                this.jda = null;
                throw new NullPointerException("Nie można odnaleźć servera o ID " + this.serverID);
            }

            this.textChannel = this.guild.getTextChannelById(this.channelID);
            if (this.textChannel == null) {
                this.jda.shutdown();
                this.jda = null;
                throw new NullPointerException("Nie można odnaleźć kanału z ID&b " + this.channelID);
            }

            this.textChannel.getManager().setSlowmode(1).queue();

            this.logChannel = this.guild.getTextChannelById(this.logID);

            if (this.logChannel == null) {
                this.logger.debug("(log) Nie można odnaleźć kanału z ID &b " + this.consoleID);
            }

            this.consoleChannel = this.guild.getTextChannelById(this.consoleID);

            if (this.consoleChannel == null) {
                this.logger.debug("(konsola) Nie można odnaleźć kanału z ID &b " + this.consoleID);
            }

            this.linkingManager = new LinkingManager(this.discordExtension, this);
            this.statsChannelsManager = new StatsChannelsManager(this.discordExtension, this);
            this.statsChannelsManager.init();

            for (final JDAListener listener : this.listeners) {
                try {
                    listener.init();
                    listener.initServerProcess(this.bdsAutoEnable.getServerProcess());
                    this.jda.addEventListener(listener);
                    this.logger.debug("Zarejestrowano listener JDA:&b " + listener.getClass().getSimpleName());
                } catch (final Exception exception) {
                    this.logger.critical("Wystąpił błąd podczas ładowania listeneru: &b" + listener.getClass().getSimpleName(), exception);
                    throw exception;
                }
            }

            this.checkBotPermissions();

            this.guild.updateCommands().addCommands(
                    Commands.slash("list", "lista graczy online."),
                    Commands.slash("backup", "Tworzenie bądź ostatni czas backupa"),
                    Commands.slash("difficulty", "Zmienia poziom trudności"),
                    Commands.slash("version", "Wersja BDS-Auto-Enable i severa, umożliwia update servera"),
                    Commands.slash("ping", "Aktualny ping bot z serwerami discord"),
                    Commands.slash("stats", "Statystyki Servera i aplikacji."),
                    Commands.slash("cmd", "Wykonuje polecenie w konsoli.")
                            .addOption(OptionType.STRING, "command", "Polecenie które zostanie wysłane do konsoli.", true),
                    Commands.slash("link", "Łączy konto Discord z kontem nickiem Minecraft.")
                            .addOption(OptionType.STRING, "code", "Kod aby połączyć konta", false),
                    Commands.slash("unlink", "Rozłącza konto Discord z kontem nickiem Minecraft")
                            .addOption(OptionType.STRING, "name", "Nick użytkownika którego konto ma zostać rozłączone", false),
                    Commands.slash("ip", "Informacje o ip ustawione w config"),
                    Commands.slash("playtime", "Top 100 graczy z największą ilością przegranego czasu"),
                    Commands.slash("deaths", "Top 100 graczy z największą ilością śmierci"),
                    Commands.slash("server", "Informacje o danym serwerze")
                            .addOption(OptionType.STRING, "ip", "Adres IP servera", true)
                            .addOption(OptionType.INTEGER, "port", "Port servera", false)
            ).queue();

            this.customStatusUpdate();
            this.leaveGuilds();
        }
    }

    private List<GatewayIntent> getGatewayIntents() {
        return Arrays.asList(
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.MESSAGE_CONTENT
        );
    }

    private void checkBotPermissions() {
        final Member botMember = this.getBotMember();
        if (botMember == null) return;

        if (!botMember.hasPermission(Permission.ADMINISTRATOR)) {
            this.logger.error("Bot nie ma uprawnień administratora , są one wymagane");
            this.sendEmbedMessage("Brak uprawnień",
                    "**Bot nie posiada uprawnień administratora**\n" +
                            "Są one wymagane do `100%` pewności że wszytko bedzie działać w nim",
                    new Footer("Brak uprawnień"));
        }
    }

    public boolean isCacheFlagEnabled(final CacheFlag cacheFlag) {
        return this.jda.getCacheFlags().contains(cacheFlag);
    }

    public void sendPrivateMessage(final User user, final String message) {
       if (user.isBot()) return;
        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
    }

    public void sendPrivateMessage(final User user, final MessageEmbed embed) {
        if (user.isBot()) return;
        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(embed).queue());
    }

    public void mute(final Member member, final long amount, final TimeUnit timeUnit) {
        if (!member.isOwner() && this.getBotMember().canInteract(member)) {
            member.timeoutFor(amount, timeUnit).queue();
        }
    }

    public void unMute(final Member member) {
        if (!member.isOwner() && member.isTimedOut() && this.getBotMember().canInteract(member)) {
            member.removeTimeout().queue();
        }
    }

    @Nullable
    public Role getHighestRole(final long memberID) {
        final Member member = this.guild.getMemberById(memberID);
        if (member == null) return null;
        Role highestRole = null;
        for (final Role role : member.getRoles()) {
            if (highestRole == null || role.getPosition() > highestRole.getPosition()) {
                highestRole = role;
            }
        }
        return highestRole;
    }

    public String getUserName(final Member member, final User author) {
        if (member != null && member.getNickname() != null) return member.getNickname();

        //Robie tak aby uzyskać custom nick gracza który ma na serwerze
        return (author != null ? author.getName() : (member != null ? member.getEffectiveName() : ""));
    }

    public String getOwnerMention() {
        if (this.guild == null) return "";
        return (this.guild.getOwner() == null ? " " : "<@" + this.guild.getOwner().getIdLong() + ">");
    }

    public String getRoleColor(final Role role) {
        if (role == null) return "";
        final Color col = role.getColor();
        return ConsoleColors.getMinecraftColorFromRGB(col.getRed(), col.getGreen(), col.getBlue());
    }

    public String getColoredRole(final Role role) {
        return role == null ? "" : (this.getRoleColor(role) + "@" + role.getName() + "&r");
    }

    public String getStatusColor(final OnlineStatus onlineStatus) {
        return switch (onlineStatus) {
            case OFFLINE -> "&7";
            case IDLE -> "&e";
            case ONLINE -> "&a";
            case INVISIBLE -> "&f";
            case DO_NOT_DISTURB -> "&4";
            case UNKNOWN -> "&0";
        };
    }

    public List<Member> getAllChannelMembers(final TextChannel textChannel) {
        return textChannel.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .sorted(Comparator.comparing(Member::getTimeJoined))
                .toList();
    }

    public List<Member> getAllChannelOnlineMembers(final TextChannel textChannel) {
        return textChannel.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE))
                .sorted(Comparator.comparing(Member::getTimeJoined))
                .toList();
    }

    private List<Member> getGuildMembers(final Guild guild) {
        return guild.getMembers().stream()
                .filter(member -> !member.getUser().isBot()).sorted(Comparator.comparing(Member::getTimeJoined)).toList();
    }

    public void setBotActivityStatus(final String activityMessage) {
        this.setBotActivityStatus(activityMessage, null);
    }

    public void setBotActivityStatus(final String activityMessage, @Nullable final Activity.ActivityType activityType) {
        this.botConfig.setActivityMessage(activityMessage);
        if (activityType != null) this.botConfig.setActivity(activityType);
        this.discordConfig.save();
        this.jda.getPresence().setActivity(DiscordJDA.this.getCustomActivity());
    }

    public Member getBotMember() {
        return this.guild.getMember(this.jda.getSelfUser());
    }

    private void customStatusUpdate() {
        final Timer timer = new Timer("Discord Status Changer", true);
        final TimerTask statusTask = new TimerTask() {

            public void run() {
                DiscordJDA.this.jda.getPresence().setActivity(DiscordJDA.this.getCustomActivity());
            }
        };

        timer.scheduleAtFixedRate(statusTask, MathUtil.minutesTo(1, TimeUnit.MILLISECONDS), MathUtil.minutesTo(10, TimeUnit.MILLISECONDS));
    }

    private Activity getCustomActivity() {
        final String replacement = String.valueOf(MathUtil.millisTo((System.currentTimeMillis() - this.bdsAutoEnable.getServerProcess().getStartTime()), TimeUnit.MINUTES));
        final String activityMessage = this.botConfig.getActivityMessage().replaceAll("<time>", replacement);

        switch (this.botConfig.getActivity()) {
            case PLAYING -> {
                return Activity.playing(activityMessage);
            }
            case WATCHING -> {
                return Activity.watching(activityMessage);
            }
            case COMPETING -> {
                return Activity.competing(activityMessage);
            }
            case LISTENING -> {
                return Activity.listening(activityMessage);
            }
            case CUSTOM_STATUS -> {
                    return Activity.customStatus(activityMessage);
            }
            case STREAMING -> {
                return Activity.streaming(activityMessage, this.botConfig.getStreamUrl());
            }
            default -> {
                this.logger.error("Wykryto nie wspierany status! ");
                return Activity.playing(activityMessage.replaceAll("<time>", replacement));
            }
        }
    }

    private void leaveGuilds() {
        if (!this.botConfig.isLeaveServers()) return;
        for (final Guild guild1 : this.jda.getGuilds()) {
            if (guild1 != this.guild) {
                String inviteLink = "";
                final DefaultGuildChannelUnion defaultChannel = guild1.getDefaultChannel();
                if (defaultChannel != null) inviteLink += defaultChannel.createInvite().complete().getUrl();

                guild1.leave().queue();
                this.sendMessage("Opuściłem serwer o ID: " + guild1.getId() +
                        "\n Nazwie: " + guild1.getName() +
                        "\n Zaproszenie: " + inviteLink
                );
            }
        }
    }

    /**
     * Kod lekko przerobiony z https://github.com/DiscordSRV/DiscordSRV/blob/master/src/main/java/github/scarsz/discordsrv/util/DiscordUtil.java#L135
     */

    private String convertMentionsFromNames(String message) {
        if (!message.contains("@")) return message;

        final Map<Pattern, String> patterns = new HashMap<>();
        for (final Role role : this.guild.getRoles()) {
            final Pattern pattern = this.mentionPatternCache.computeIfAbsent(
                    role.getId(),
                    mentionable -> Pattern.compile(
                            "(?<!<)" +
                                    Pattern.quote("@" + role.getName()),
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
                    )
            );
            if (!role.isMentionable()) continue;
            patterns.put(pattern, role.getAsMention());
        }

        for (final Member member : this.guild.getMembers()) {
            final Pattern pattern = this.mentionPatternCache.computeIfAbsent(
                    member.getId(),
                    mentionable -> Pattern.compile(
                            "(?<!<)" +
                                    Pattern.quote("@" + member.getEffectiveName()),
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
                    )
            );
            patterns.put(pattern, member.getAsMention());
        }

        for (final Map.Entry<Pattern, String> entry : patterns.entrySet()) {
            message = entry.getKey().matcher(message).replaceAll(entry.getValue());
        }

        return this.removeRoleMention(message);
    }

    private String removeRoleMention(String message) {
        for (final Role role : this.guild.getRoles()) {
            final String rolePattern = "<@&" + role.getId() + ">";
            if (message.contains(rolePattern) && !role.isMentionable()) {
                message = message.replaceAll(rolePattern, role.getName());
            }
        }
        return message;
    }

    public void sendMessage(final String message) {
        if (this.jda != null && this.textChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            if (message.isEmpty()) return;
            this.textChannel.sendMessage(
                    this.convertMentionsFromNames(message).replaceAll("<owner>", this.getOwnerMention())
            ).queue();
        }
    }

    public void sendMessage(final String message, final Throwable throwable) {
        this.sendMessage(message +
                (throwable == null ? "" : "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```"));
    }

    public void sendEmbedMessage(final String title, final String message, final List<Field> fields, final Footer footer) {
        if (this.jda != null && this.textChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            if (title.isEmpty() || message.isEmpty()) return;
            final EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(title)
                    .setDescription(message.replaceAll("<owner>", this.getOwnerMention()))
                    .setColor(Color.BLUE)
                    .setFooter(footer.text(), footer.imageURL());

            if (fields != null && !fields.isEmpty()) {
                for (final Field field : fields) {
                    embed.addField(field.name(), field.value(), field.inline());
                }
            }

            this.textChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    public void sendEmbedMessage(final String title, final String message, final List<Field> fields, final Throwable throwable, final Footer footer) {
        this.sendEmbedMessage(title, message +
                (throwable == null ? "" : "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```"), fields, footer);
    }

    public void sendEmbedMessage(final String title, final String message, final Footer footer) {
        this.sendEmbedMessage(title, message, (List<Field>) null, footer);
    }

    public void sendEmbedMessage(final String title, final String message, final Throwable throwable, final Footer footer) {
        this.sendEmbedMessage(title, message +
                (throwable == null ? "" : "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```"), footer);
    }

    public void log(final String title, final String message, final List<Field> fields, final Footer footer) {
        if (this.jda != null && this.logChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            if (title.isEmpty() || message.isEmpty()) return;
            final EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(title)
                    .setDescription(message.replaceAll("<owner>", this.getOwnerMention()))
                    .setColor(Color.BLUE)
                    .setFooter(footer.text(), footer.imageURL());

            if (fields != null && !fields.isEmpty()) {
                for (final Field field : fields) {
                    embed.addField(field.name(), field.value(), field.inline());
                }
            }
            this.logChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    public void log(final String title, final String message, final Footer footer) {
        this.log(title, message, (List<Field>) null, footer);
    }

    public void writeConsole(final String message) {
        if (this.jda != null && this.consoleChannel != null && this.jda.getStatus() == JDA.Status.CONNECTED) {
            if (message.isEmpty()) return;
            this.consoleChannel.sendMessage(message.replaceAll("<owner>", this.getOwnerMention())).queue();
        }
    }

    public void writeConsole(final String message, final Throwable throwable) {
        this.writeConsole(message + (throwable == null ? "" : "\n```" + MessageUtil.getStackTraceAsString(throwable) + "```"));
    }

    public void sendJoinMessage(final String playerName) {
        if (this.discordConfig.getMessagesOptionsConfig().isSendJoinMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getJoinMessage().replaceAll("<name>", playerName));
        }
    }

    public void sendLeaveMessage(final String playerName) {
        if (this.discordConfig.getMessagesOptionsConfig().isSendLeaveMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getLeaveMessage().replaceAll("<name>", playerName));
        }
    }

    public void sendPlayerMessage(final String playerName, final String playerMessage) {
        if (this.discordConfig.getMessagesOptionsConfig().isSendPlayerMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getMinecraftToDiscordMessage()
                    .replaceAll("<name>", playerName)
                    .replaceAll("<msg>", playerMessage.replaceAll("\\\\", ""))
                    .replaceAll("@everyone", "/everyone/")
                    .replaceAll("@here", "/here/")
            );
        }
    }

    public void sendDeathMessage(final String playerName, final String deathMessage) {
        if (this.discordConfig.getMessagesOptionsConfig().isSendDeathMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getDeathMessage()
                    .replaceAll("<name>", playerName)
                    .replaceAll("<deathMessage>", deathMessage.replaceAll("\\\\", ""))
            );
        }
    }

    public void sendDisabledMessage() {
        if (this.discordConfig.getMessagesOptionsConfig().isSendDisabledMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getDisabledMessage());
        }
    }

    public void sendDisablingMessage() {
        if (this.discordConfig.getMessagesOptionsConfig().isSendDisablingMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getDisablingMessage());
        }
    }

    public void sendProcessEnabledMessage() {
        if (this.discordConfig.getMessagesOptionsConfig().isSendProcessEnabledMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getProcessEnabledMessage());
        }
    }

    public void sendEnabledMessage() {
        if (this.discordConfig.getMessagesOptionsConfig().isSendEnabledMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getEnabledMessage());
        }
    }

    public void sendDestroyedMessage() {
        if (this.discordConfig.getMessagesOptionsConfig().isSendDestroyedMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getDestroyedMessage());
        }
    }

    public void sendBackupDoneMessage() {
        if (this.discordConfig.getMessagesOptionsConfig().isSendBackupMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getBackupDoneMessage());
        }
    }

    public void sendBackupFailMessage(final Exception exception) {
        if (this.discordConfig.getMessagesOptionsConfig().isSendBackupFailMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getBackupFailMessage());
        }
    }

    public void sendAppRamAlert() {
        if (this.appConfigManager.getWatchDogConfig().getRamMonitorConfig().isDiscordAlters()) {
            this.sendMessage(this.getOwnerMention() + this.discordConfig.getMessagesConfig().getAppRamAlter());
        }
    }

    public void sendMachineRamAlert() {
        if (this.appConfigManager.getWatchDogConfig().getRamMonitorConfig().isDiscordAlters()) {
            this.sendMessage(this.getOwnerMention() + this.discordConfig.getMessagesConfig().getMachineRamAlter());
        }
    }

    public void sendServerUpdateMessage(final String version) {
        if (this.discordConfig.getMessagesOptionsConfig().isSendServerUpdateMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getServerUpdate()
                    .replaceAll("<version>", version)
                    .replaceAll("<current>", this.appConfigManager.getVersionManagerConfig().getVersion())

            );
        }
    }

    public void sendRestartMessage() {
        if (this.discordConfig.getMessagesOptionsConfig().isSendRestartMessage()) {
            this.sendMessage(this.discordConfig.getMessagesConfig().getRestartMessage());
        }
    }

    public JDA getJda() {
        return this.jda;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public TextChannel getTextChannel() {
        return this.textChannel;
    }

    public TextChannel getConsoleChannel() {
        return this.consoleChannel;
    }

    @Nullable
    public StatsChannelsManager getStatsChannelsManager() {
        return this.statsChannelsManager;
    }

    @Nullable
    public LinkingManager getLinkingManager() {
        return this.linkingManager;
    }
}