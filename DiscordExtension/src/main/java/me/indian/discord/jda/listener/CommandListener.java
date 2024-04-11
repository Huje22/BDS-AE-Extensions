package me.indian.discord.jda.listener;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.config.AppConfigManager;
import me.indian.bds.logger.LogState;
import me.indian.bds.logger.Logger;
import me.indian.bds.player.Controller;
import me.indian.bds.player.DeviceOS;
import me.indian.bds.player.PlayerStatistics;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.server.allowlist.AllowlistManager;
import me.indian.bds.server.allowlist.component.AllowlistPlayer;
import me.indian.bds.server.properties.component.Difficulty;
import me.indian.bds.server.properties.component.Gamemode;
import me.indian.bds.server.stats.ServerStats;
import me.indian.bds.server.stats.StatsManager;
import me.indian.bds.util.BedrockQuery;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.MathUtil;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.bds.util.geyser.GeyserUtil;
import me.indian.bds.version.VersionManager;
import me.indian.bds.watchdog.module.BackupModule;
import me.indian.bds.watchdog.module.pack.PackModule;
import me.indian.discord.DiscordExtension;
import me.indian.discord.config.DiscordConfig;
import me.indian.discord.config.sub.BotConfig;
import me.indian.discord.embed.component.Footer;
import me.indian.discord.jda.DiscordJDA;
import me.indian.discord.jda.manager.LinkingManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CommandListener extends ListenerAdapter implements JDAListener {

    private final DiscordExtension discordExtension;
    private final DiscordJDA discordJDA;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final AppConfigManager appConfigManager;
    private final DiscordConfig discordConfig;
    private final BotConfig botConfig;
    private final List<Button> backupButtons, difficultyButtons, statsButtons;
    private final List<String> allowlistPlayers;
    private final ExecutorService service;
    private final ServerProcess serverProcess;
    private JDA jda;
    private StatsManager statsManager;
    private BackupModule backupModule;
    private PackModule packModule;
    private LinkingManager linkingManager;

    public CommandListener( final DiscordExtension discordExtension) {
        this.discordExtension = discordExtension;
        this.discordJDA = this.discordExtension.getDiscordJDA();
        this.bdsAutoEnable = discordExtension.getBdsAutoEnable();
        this.logger = discordExtension.getLogger();
        this.appConfigManager = this.bdsAutoEnable.getAppConfigManager();
        this.discordConfig = discordExtension.getConfig();
        this.botConfig = this.discordConfig.getBotConfig();
        this.backupButtons = new LinkedList<>();
        this.difficultyButtons = new LinkedList<>();
        this.statsButtons = new LinkedList<>();
        this.allowlistPlayers = new LinkedList<>();
        this.service = Executors.newScheduledThreadPool(3, new ThreadUtil("Discord Command Listener"));
        this.serverProcess = this.bdsAutoEnable.getServerProcess();
    }

    @Override
    public void init() {
        this.jda = this.discordJDA.getJda();
        this.statsManager = this.bdsAutoEnable.getServerManager().getStatsManager();
        this.backupModule = this.bdsAutoEnable.getWatchDog().getBackupModule();
        this.packModule = this.bdsAutoEnable.getWatchDog().getPackModule();
        this.linkingManager = this.discordJDA.getLinkingManager();
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;
        final User author = member.getUser();

        this.service.execute(() -> {
            //Robie to na paru wątkach gdyby jakieś polecenie miało zblokować ten od JDA
            try {
                event.deferReply().setEphemeral(true).queue();

                switch (event.getName()) {
                    case "cmd" -> {
                        if (member.hasPermission(Permission.MANAGE_SERVER)) {
                            if (!this.serverProcess.isEnabled()) {
                                event.getHook().editOriginal("Server jest wyłączony").queue();
                                return;
                            }

                            final String command = event.getOption("command").getAsString();
                            if (command.isEmpty()) {
                                event.getHook().editOriginal("Polecenie nie może być puste!").queue();
                                return;
                            }

                            this.logger.print(command);
                            this.discordJDA.writeConsole(command);

                            final MessageEmbed embed = new EmbedBuilder()
                                    .setTitle("Ostatnia linijka z konsoli")
                                    .setDescription(this.serverProcess.commandAndResponse(command))
                                    .setColor(Color.BLUE)
                                    .setFooter("Używasz: " + command)
                                    .build();

                            this.discordJDA.log("Użycie polecenia",
                                    "**" + member.getEffectiveName() + "** (" + member.getIdLong() + ")",
                                    new Footer(command));

                            event.getHook().editOriginalEmbeds(embed).queue();
                        } else {
                            event.getHook().editOriginal("Nie posiadasz permisji!!").queue();
                        }
                    }

                    case "link" -> {
                        if (!this.packModule.isLoaded()) {
                            event.getHook().editOriginal("Paczka **" + this.packModule.getPackName() + "** nie została załadowana").queue();
                            return;
                        }
                        final OptionMapping codeMapping = event.getOption("code");
                        if (codeMapping != null && !codeMapping.getAsString().isEmpty()) {
                            final String code = codeMapping.getAsString();
                            final long id = member.getIdLong();

                            final EmbedBuilder linkingEmbed = new EmbedBuilder().setTitle("Łączenie kont").setColor(Color.BLUE);

                            if (this.linkingManager.isCanUnlink()) {
                                linkingEmbed.setFooter("Aby rozłączyć konto wpisz /unlink");
                            }

                            if (this.linkingManager.isLinked(id)) {
                                linkingEmbed.setDescription("Twoje konto jest już połączone z: **" + this.linkingManager.getNameByID(id) + "**" + this.hasEnoughHours(member));
                                event.getHook().editOriginalEmbeds(linkingEmbed.build()).queue();
                                return;
                            }

                            if (this.linkingManager.linkAccount(code, id)) {
                                this.discordJDA.log("Połączenie kont",
                                        "Użytkownik **" + author.getName() + "** połączył konta",
                                        new Footer(author.getName() + " " + DateUtil.getTimeHMS(), member.getEffectiveAvatarUrl()));


                                linkingEmbed.setDescription("Połączono konto z nickiem: **" + this.linkingManager.getNameByID(id) + "**" + this.hasEnoughHours(member));
                                event.getHook().editOriginalEmbeds(linkingEmbed.build()).queue();
                                this.serverProcess.tellrawToPlayer(this.linkingManager.getNameByID(id),
                                        "&aPołączono konto z ID:&b " + id);
                            } else {
                                event.getHook().editOriginal("Kod nie jest poprawny").queue();
                            }
                        } else {
                            final List<String> linkedAccounts = this.getLinkedAccounts();
                            String linkedAccsString = "Już **" + linkedAccounts.size() + "** osób połączyło konta\n" + MessageUtil.listToSpacedString(linkedAccounts);

                            if (linkedAccsString.isEmpty()) {
                                linkedAccsString = "**Brak połączonych kont**";
                            }

                            final MessageEmbed messageEmbed = new EmbedBuilder()
                                    .setTitle("Osoby z połączonym kontami")
                                    .setDescription(linkedAccsString)
                                    .setColor(Color.BLUE)
                                    .setFooter("Aby połączyć konto wpisz /link KOD")
                                    .build();

                            event.getHook().editOriginalEmbeds(messageEmbed).queue();
                        }
                    }

                    case "unlink" -> {
                        final OptionMapping nameOption = event.getOption("name");
                        final EmbedBuilder unlinkEmbed = new EmbedBuilder()
                                .setTitle("Rozłączasz konto Discord z nickiem Minecraft")
                                .setColor(Color.BLUE)
                                .setFooter("Aby znów połączyć konto wpisz /link KOD");

                        if (nameOption != null) {
                            if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                                event.getHook().editOriginal("Nie masz permisji!").queue();
                                return;
                            }
                            final String name = nameOption.getAsString();

                            if (this.linkingManager.isLinked(name)) {
                                this.linkingManager.unLinkAccount(name);
                                unlinkEmbed.setDescription("Rozłączono konto " + name);
                            } else {
                                unlinkEmbed.setDescription("Gracz **" + name + "** nie posiada połączonych kont");
                            }

                        } else if (this.linkingManager.isCanUnlink() || member.hasPermission(Permission.ADMINISTRATOR)) {
                            if (this.linkingManager.isLinked(member.getIdLong())) {
                                this.linkingManager.unLinkAccount(member.getIdLong());
                                unlinkEmbed.setDescription("Rozłączono konto z nickiem **" + this.linkingManager.getNameByID(member.getIdLong()) + "**");
                            } else {
                                unlinkEmbed.setDescription("Nie posiadasz połączonego konta Discord z nickiem Minecraft");
                            }
                        } else {
                            event.getHook().editOriginal("Nie możesz sobie sam rozłączyć kont").queue();
                            return;
                        }

                        event.getHook().editOriginalEmbeds(unlinkEmbed.build()).queue();
                    }

                    case "ping" -> {
                        final MessageEmbed embed = new EmbedBuilder()
                                .setTitle("Ping Bot <-> Discord")
                                .setDescription("Aktualny ping z serverami discord wynosi: " + this.jda.getGatewayPing() + " ms")
                                .setColor(Color.BLUE)
                                .build();

                        event.getHook().editOriginalEmbeds(embed).queue();
                    }

                    case "ip" -> {
                        final MessageEmbed embed = new EmbedBuilder()
                                .setTitle("Nasze ip!")
                                .setDescription(MessageUtil.listToSpacedString(this.botConfig.getIpMessage()))
                                .setColor(Color.BLUE)
                                .build();

                        event.getHook().editOriginalEmbeds(embed).queue();
                    }

                    case "stats" -> {
                        if (member.hasPermission(Permission.MANAGE_SERVER)) {
                            event.getHook().editOriginalEmbeds(this.getStatsEmbed())
                                    .setActionRow(ActionRow.of(this.statsButtons).getComponents())
                                    .queue();
                        } else {
                            event.getHook().editOriginalEmbeds(this.getStatsEmbed()).queue();
                        }
                    }

                    case "list" -> {
                        if (!this.packModule.isLoaded()) {
                            event.getHook().editOriginal("Paczka **" + this.packModule.getPackName() + "** nie została załadowana").queue();
                            return;
                        }

                        final List<String> players = this.bdsAutoEnable.getServerManager().getOnlinePlayers();
                        final String list = "`" + MessageUtil.stringListToString(players, "`, `") + "`";
                        final int maxPlayers = this.bdsAutoEnable.getServerProperties().getMaxPlayers();
                        final EmbedBuilder embed = new EmbedBuilder()
                                .setTitle("Lista Graczy")
                                .setColor(Color.BLUE);

                        if (this.botConfig.isAdvancedPlayerList()) {
                            if (!players.isEmpty()) {
                                int counter = 0;

                                embed.setDescription("Aktualnie gra **" + players.size() + "/" + maxPlayers + "** osób");
                                for (final String player : players) {
                                    if (counter != 24) {
                                        embed.addField(player,
                                                "> Czas gry: **" + DateUtil.formatTime(this.statsManager.getPlayTime(player), List.of('d', 'h', 'm', 's'))
                                                        + "**  \n> Śmierci:** " + this.statsManager.getDeaths(player) + "**",
                                                true);
                                        counter++;
                                    } else {
                                        embed.addField("**I pozostałe**", players.size() - 24 + " osób", false);
                                        break;
                                    }
                                }
                            } else {
                                embed.setDescription("**Brak osób online**");
                            }
                        } else {
                            embed.setDescription(players.size() + "/" + maxPlayers + "\n" + (players.isEmpty() ? "**Brak osób online**" : list) + "\n");
                        }
                        event.getHook().editOriginalEmbeds(embed.build()).queue();
                    }

                    case "playerinfo" -> {
                        final OptionMapping mention = event.getOption("player");
                        if (mention != null) {
                            final Member playerMember = mention.getAsMember();
                            if (playerMember != null) {
                                final String discordPlayerName = this.discordJDA.getUserName(playerMember, playerMember.getUser());
                                final long id = playerMember.getIdLong();
                                if (this.linkingManager.isLinked(id)) {
                                    final String playerName = this.linkingManager.getNameByID(id);

                                    if (playerName == null) {
                                        event.getHook().editOriginal("Nie udało się pozyskać informacji na temat **" + discordPlayerName + "**").queue();
                                        return;
                                    }

                                    final PlayerStatistics player = this.statsManager.getPlayer(playerName);

                                    if (player == null) {
                                        event.getHook().editOriginal("Nie udało się pozyskać informacji na temat **" + playerName + "**").queue();
                                        return;
                                    }

                                    final long xuid = player.getXuid();
                                    final EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setTitle("Informacje o graczu " + this.linkingManager.getNameByID(id)).setColor(Color.BLUE);


                                    embedBuilder.setThumbnail(GeyserUtil.getBedrockSkinHead(xuid));
                                    embedBuilder.addField("Nick", playerName, true);
                                    embedBuilder.addField("XUID", String.valueOf(xuid), true);

                                    final DeviceOS deviceOS = player.getLastDevice();
                                    final Controller controller = player.getLastController();

                                    if (deviceOS != DeviceOS.UNKNOWN || controller != Controller.UNKNOWN) {
                                        embedBuilder.addBlankField(false);
                                        embedBuilder.addField("Platforma", deviceOS.toString(), true);
                                        embedBuilder.addField("Kontroler", controller.toString(), true);
                                    }

                                    final List<String> oldNames = player.getOldNames();
                                    if (oldNames != null && !oldNames.isEmpty()) {
                                        embedBuilder.addField("Znany również jako", MessageUtil.stringListToString(oldNames, " ,"), false);
                                    } else {
                                        embedBuilder.addField("Znany również jako", "__Brak danych o innych nick__", false);
                                    }

                                    final long firstJoin = player.getFirstJoin();
                                    final long lastJoin = player.getLastJoin();
                                    final long lastQuit = player.getLastQuit();

                                    if (firstJoin != 0 && firstJoin != -1) {
                                        embedBuilder.addField("Pierwsze dołączenie", this.getTime(DateUtil.longToLocalDateTime(firstJoin)), true);
                                    }
                                    if (lastJoin != 0 && lastJoin != -1) {
                                        embedBuilder.addField("Ostatnie dołączenie", this.getTime(DateUtil.longToLocalDateTime(lastJoin)), true);
                                    }

                                    if (lastQuit != 0 && lastQuit != -1) {
                                        embedBuilder.addField("Ostatnie opuszczenie", this.getTime(DateUtil.longToLocalDateTime(lastQuit)), true);
                                    }

                                    embedBuilder.addField("Login Streak", String.valueOf(player.getLoginStreak()) , true);
                                    embedBuilder.addField("Longest Login Streak", String.valueOf(player.getLoginStreak()) , true);

                                    embedBuilder.addField("Śmierci", String.valueOf(player.getDeaths()), false);
                                    embedBuilder.addField("Czas gry", DateUtil.formatTime(player.getPlaytime(), List.of('d', 'h', 'm', 's')), false);
                                    embedBuilder.addField("Postawione bloki", String.valueOf(player.getBlockPlaced()), true);
                                    embedBuilder.addField("Zniszczone bloki", String.valueOf(player.getBlockBroken()), true);

                                    String footer = "";

                                    if (this.bdsAutoEnable.getServerProperties().isAllowList()) {
                                        if (this.bdsAutoEnable.getAllowlistManager().isOnAllowList(playerName)) {
                                            footer = "Znajduje się na białej liście";
                                        } else {
                                            footer = "Nie znajduje się na białej liście";
                                        }
                                    }

                                    embedBuilder.setFooter(footer, GeyserUtil.getBedrockSkinBody(xuid));
                                    event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
                                } else {
                                    event.getHook()
                                            .editOriginal("Użytkownik **" + discordPlayerName + "** nie posiada połączonych kont")
                                            .queue();
                                }
                            } else {
                                event.getHook().editOriginal("Podany gracz jest nieprawidłowy").queue();
                            }
                        }
                    }

                    case "allowlist" -> {
                        final AllowlistManager allowlistManager = this.bdsAutoEnable.getAllowlistManager();
                        final OptionMapping addOption = event.getOption("add");
                        final OptionMapping removeOption = event.getOption("remove");

                        if (!this.bdsAutoEnable.getServerProperties().isAllowList()) {
                            event.getHook().sendMessage("Allowlista jest __wyłączona__").setEphemeral(true).queue();
                            return;
                        }

                        if (addOption == null && removeOption == null) {
                            final EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Biała lista").setColor(Color.BLUE);

                            this.allowlistPlayers.clear();
                            for (final AllowlistPlayer player : allowlistManager.getAllowlistPlayers()) {
                                this.allowlistPlayers.add(player.name());
                            }

                            if (this.allowlistPlayers.isEmpty()) {
                                embedBuilder.setDescription("**Nikt jeszcze nie jest na allowlist**");
                            } else {
                                embedBuilder.setDescription("Aktualnie na białej liście znajduje się **" + this.allowlistPlayers.size() + "** osób \n" +
                                        MessageUtil.stringListToString(this.allowlistPlayers, " , "));
                            }

                            event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
                            return;
                        }

                        if (!member.hasPermission(Permission.MODERATE_MEMBERS)) {
                            event.getHook().editOriginal("Potrzebujesz uprawnienia: **" + Permission.MODERATE_MEMBERS.getName() + "**").queue();
                            return;
                        }

                        if (addOption != null) {
                            final String playerName = addOption.getAsString();
                            if (allowlistManager.isOnAllowList(playerName)) {
                                event.getHook().editOriginal("Gracz **" + playerName + "** jest już na liście").queue();
                            } else {
                                allowlistManager.addPlayerByName(playerName);
                                allowlistManager.saveAllowlist();
                                if (this.serverProcess.isEnabled()) {
                                    allowlistManager.reloadAllowlist();
                                }
                                event.getHook().editOriginal("Dodano gracza **" + playerName + "**").queue();
                            }
                        } else if (removeOption != null) {
                            final String playerName = removeOption.getAsString();
                            if (allowlistManager.isOnAllowList(playerName)) {
                                //Jeśli gracz jest na liście nie może być null :P
                                final AllowlistPlayer player = allowlistManager.getPlayer(playerName);
                                allowlistManager.removePlayer(player);
                                allowlistManager.saveAllowlist();
                                if (this.serverProcess.isEnabled()) {
                                    allowlistManager.reloadAllowlist();
                                }
                                event.getHook().editOriginal("Usunięto gracza **" + playerName + "**").queue();
                            } else {
                                event.getHook().editOriginal("Gracz **" + playerName + "** nie jest na liście").queue();
                            }
                        }
                    }

                    case "backup" -> {
                        if (!this.bdsAutoEnable.getAppConfigManager().getWatchDogConfig().getBackupConfig().isEnabled()) {
                            event.getHook().editOriginal("Backupy są wyłączone")
                                    .queue();
                            return;
                        }
                        if (member.hasPermission(Permission.ADMINISTRATOR)) {
                            event.getHook().editOriginalEmbeds(this.getBackupEmbed())
                                    .setActionRow(ActionRow.of(this.backupButtons).getComponents())
                                    .queue();
                        } else {
                            event.getHook().editOriginalEmbeds(this.getBackupEmbed()).queue();
                        }
                    }

                    case "top" -> {
                        if (!this.packModule.isLoaded()) {
                            event.getHook().editOriginal("Paczka **" + this.packModule.getPackName() + "** nie została załadowana").queue();
                            return;
                        }

                        final EmbedBuilder embed = new EmbedBuilder()
                                .setTitle("Statystyki graczy")
                                .setColor(Color.BLUE);


                        event.getHook().editOriginalEmbeds(embed.build())
                                .setActionRow(ActionRow.of(
                                        Button.primary("playtime", "Czas gry").withEmoji(Emoji.fromFormatted("<a:animated_clock:562493945058164739>")),
                                        Button.primary("deaths", "Śmierci").withEmoji(Emoji.fromUnicode("☠️")),
                                        Button.primary("blocks", "Bloki").withEmoji(Emoji.fromFormatted("<:kilof:1064228602759102464>"))
                                ).getComponents())
                                .queue();
                    }

                    case "difficulty" -> {
                        if (member.hasPermission(Permission.ADMINISTRATOR)) {
                            event.getHook().editOriginalEmbeds(this.getDifficultyEmbed())
                                    .setActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                                    .queue();
                        } else {
                            event.getHook().editOriginalEmbeds(this.getDifficultyEmbed()).queue();
                        }
                    }

                    case "version" -> {
                        final VersionManager versionManager = this.bdsAutoEnable.getVersionManager();
                        final String current = versionManager.getLoadedVersion();
                        final int protocol = versionManager.getLastKnownProtocol();
                        String latest = versionManager.getLatestVersion();
                        if (latest.equals("")) {
                            latest = current;
                        }

                        final String checkLatest = (current.equals(latest) ? "`" + latest + "` (" + protocol + ")" : "`" + current + "` (" + protocol + ") (Najnowsza to: `" + latest + "`)");

                        final MessageEmbed embed = new EmbedBuilder()
                                .setTitle("Informacje o wersji")
                                .setDescription("**Wersjia __BDS-Auto-Enable__**: `" + this.bdsAutoEnable.getProjectVersion() + "`\n" +
                                        "**Wersjia Servera **: " + checkLatest + "\n"
                                )
                                .setColor(Color.BLUE)
                                .build();

                        if (member.hasPermission(Permission.ADMINISTRATOR)) {
                            Button button = Button.primary("update", "Update")
                                    .withEmoji(Emoji.fromUnicode("\uD83D\uDD3C"));
                            if (current.equals(latest)) {
                                button = button.asDisabled();
                            } else {
                                button = button.asEnabled();
                            }

                            event.getHook().editOriginalEmbeds(embed).setActionRow(button).queue();
                        } else {
                            event.getHook().editOriginalEmbeds(embed).queue();
                        }
                    }

                    case "server" -> {
                        final OptionMapping portOption = event.getOption("port");
                        final String adres = event.getOption("ip").getAsString();
                        int port = 19132;

                        if (portOption != null) port = portOption.getAsInt();

                        event.getHook().editOriginalEmbeds(this.getServerInfoEmbed(adres, port)).queue();
                    }

                    default ->
                            event.getHook().editOriginal("Polecenie **" + event.getName() + "** nie jest jeszcze przez nas obsługiwane").queue();
                }
            } catch (final Exception exception) {
                this.logger.error("Wystąpił błąd przy próbie wykonania&b " + event.getName() + "&r przez&e " + member.getNickname(), exception);
                event.getHook().editOriginal("Wystąpił błąd\n ```" + MessageUtil.getStackTraceAsString(exception) + "```").queue();
            }
        });
    }

    @Override
    public void onButtonInteraction(final ButtonInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;

        event.deferReply().setEphemeral(true).queue();

        if (this.serverTopButton(event)) return;

        if (member.hasPermission(Permission.MANAGE_SERVER)) {
            this.serveUpdateButton(event);
            this.serveStatsButtons(event);
            this.serveBackupButton(event);
            this.serveDeleteBackupButton(event);
            this.serveDifficultyButton(event);
            return;
        }

        event.getHook().editOriginal("Nie posiadasz permisji").queue();
    }

    public String getTime(final LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy\nHH:mm:ss"));
    }

    private List<String> getLinkedAccounts() {
        final Map<Long, Long> linkedAccounts = this.linkingManager.getLinkedAccounts();
        final List<String> linked = new LinkedList<>();
        int place = 1;

        for (final Map.Entry<Long, Long> entry : linkedAccounts.entrySet()) {
            final String playerName = this.statsManager.getNameByXuid(entry.getKey());
            final long hours = MathUtil.hoursFrom(this.bdsAutoEnable.getServerManager().getStatsManager()
                    .getPlayTime(playerName), TimeUnit.MILLISECONDS);

            linked.add(place + ". **" + playerName + "**: " + entry.getValue() + " " + (hours < 5 ? "❌" : "✅"));
            place++;
        }

        return linked;
    }

    private String hasEnoughHours(final Member member) {
        String hoursMessage = "";
        final long roleID = this.discordExtension.getLinkingConfig().getLinkedPlaytimeRoleID();
        final long hours = MathUtil.hoursFrom(this.bdsAutoEnable.getServerManager().getStatsManager()
                .getPlayTime(this.linkingManager.getNameByID(member.getIdLong())), TimeUnit.MILLISECONDS);
        if (hours < 5) {
            if (this.jda.getRoleById(roleID) != null) {
                hoursMessage = "\nMasz za mało godzin gry aby otrzymać <@&" + roleID + "> (**" + hours + "** godzin gry)" +
                        "\nDostaniesz role gdy wbijesz **5** godzin gry";
            }
        }

        return hoursMessage;
    }

    private boolean serverTopButton(final ButtonInteractionEvent event) {
        switch (event.getComponentId()) {
            case "playtime" -> {
                event.getHook().editOriginalEmbeds(this.getPlaytimeEmbed()).queue();
                return true;
            }
            case "deaths" -> {
                event.getHook().editOriginalEmbeds(this.getDeathsEmbed()).queue();
                return true;
            }
            case "blocks" -> {
                event.getHook().editOriginalEmbeds(this.getTopBlockEmbed()).queue();
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void serveDifficultyButton(final ButtonInteractionEvent event) {
        switch (event.getComponentId()) {
            case "peaceful" -> {
                this.serverProcess.sendToConsole("difficulty " + Difficulty.PEACEFUL.getDifficultyName());
                this.bdsAutoEnable.getServerProperties().setDifficulty(Difficulty.PEACEFUL);
                event.getHook().editOriginalEmbeds(this.getDifficultyEmbed())
                        .setActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .queue();
            }
            case "easy" -> {
                this.serverProcess.sendToConsole("difficulty " + Difficulty.EASY.getDifficultyName());
                this.bdsAutoEnable.getServerProperties().setDifficulty(Difficulty.EASY);
                event.getHook().editOriginalEmbeds(this.getDifficultyEmbed())
                        .setActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .queue();
            }
            case "normal" -> {
                this.serverProcess.sendToConsole("difficulty " + Difficulty.NORMAL.getDifficultyName());
                this.bdsAutoEnable.getServerProperties().setDifficulty(Difficulty.NORMAL);
                event.getHook().editOriginalEmbeds(this.getDifficultyEmbed())
                        .setActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .queue();
            }
            case "hard" -> {
                this.serverProcess.sendToConsole("difficulty " + Difficulty.HARD.getDifficultyName());
                this.bdsAutoEnable.getServerProperties().setDifficulty(Difficulty.HARD);
                event.getHook().editOriginalEmbeds(this.getDifficultyEmbed())
                        .setActionRow(ActionRow.of(this.difficultyButtons).getComponents())
                        .queue();
            }
        }
    }

    private void serveDeleteBackupButton(final ButtonInteractionEvent event) {
        for (final Path path : this.backupModule.getBackups()) {
            final String fileName = path.getFileName().toString();
            if (event.getComponentId().equals("delete_backup:" + fileName)) {
                try {
                    if (!Files.deleteIfExists(path)) {
                        event.getHook().editOriginal("Nie udało się usunąć backupa " + fileName).queue();
                        return;
                    }
                    this.backupModule.getBackups().remove(path);
                    event.getHook().editOriginalEmbeds(this.getBackupEmbed())
                            .setActionRow(ActionRow.of(this.backupButtons).getComponents())
                            .queue();
                    this.serverProcess.tellrawToAllAndLogger("&7[&bDiscord&7]",
                            "&aUżytkownik&b " + this.discordJDA.getUserName(event.getMember(), event.getUser()) +
                                    "&a usunął backup&b " + fileName + "&a za pomocą&e discord"
                            , LogState.INFO);

                    return;
                } catch (final Exception exception) {
                    event.getHook().editOriginal("Nie udało się usunąć backupa " + fileName + " " + exception.getMessage()).queue();
                }
            }
        }
    }

    private void serveBackupButton(final ButtonInteractionEvent event) {
        if (event.getComponentId().equals("backup")) {
            this.service.execute(() -> {
                if (this.backupModule.isBackuping()) {
                    event.getHook().editOriginal("Backup jest już robiony!").queue();
                    return;
                }
                if (!this.serverProcess.isEnabled()) {
                    event.getHook().editOriginal("Server jest wyłączony!").queue();
                    return;
                }

                this.backupModule.backup();
                ThreadUtil.sleep((int) this.appConfigManager.getWatchDogConfig().getBackupConfig().getLastBackupTime() + 3);
                event.getHook().editOriginalEmbeds(this.getBackupEmbed())
                        .setActionRow(this.backupButtons).queue();
            });
        }
    }

    private void serveUpdateButton(final ButtonInteractionEvent event) {
        this.service.execute(() -> {
            if (event.getComponentId().equals("update")) {
                event.getHook().editOriginal("Server jest już prawdopodobnie aktualizowany , jeśli nie zajrzyj w konsole")
                        .queue();
                this.bdsAutoEnable.getVersionManager().getVersionUpdater().updateToLatest();
            }
        });
    }

    private void serveStatsButtons(final ButtonInteractionEvent event) {
        if (!event.getComponentId().contains("stats_")) return;
        final Member member = event.getMember();
        switch (event.getComponentId()) {
            case "stats_enable" -> {
                this.serverProcess.setCanRun(true);
                this.serverProcess.startProcess();

                this.discordJDA.log("Włączenie servera",
                        "**" + member.getEffectiveName() + "** (" + member.getIdLong() + ")",
                        new Footer(""));
            }
            case "stats_disable" -> {
                this.serverProcess.setCanRun(false);
                this.serverProcess.kickAllPlayers("&aServer został wyłączony za pośrednictwem&b discord");
                this.serverProcess.sendToConsole("stop");

                this.discordJDA.log("Wyłączenie servera",
                        "**" + member.getEffectiveName() + "** (" + member.getIdLong() + ")",
                        new Footer(""));
            }
        }
        ThreadUtil.sleep(3);
        event.getHook().editOriginalEmbeds(this.getStatsEmbed())
                .setActionRow(ActionRow.of(this.statsButtons).getComponents())
                .queue();
    }

    private MessageEmbed getPlaytimeEmbed() {
        final List<String> playTime = StatusUtil.getTopPlayTime(true, 100);
        final ServerStats serverStats = this.bdsAutoEnable.getServerManager().getStatsManager().getServerStats();
        final String totalUpTime = "Łączny czas działania servera: "
                + DateUtil.formatTime(serverStats.getTotalUpTime(), List.of('d', 'h', 'm', 's'));

        return new EmbedBuilder()
                .setTitle("Top 100 graczy z największą ilością przegranego czasu")
                .setDescription((playTime.isEmpty() ? "**Brak Danych**" : MessageUtil.listToSpacedString(playTime)))
                .setColor(Color.BLUE)
                .setFooter(totalUpTime)
                .build();
    }

    private MessageEmbed getDeathsEmbed() {
        final List<String> deaths = StatusUtil.getTopDeaths(true, 100);
        return new EmbedBuilder()
                .setTitle("Top 100 graczy z największą ilością śmierci")
                .setDescription((deaths.isEmpty() ? "**Brak Danych**" : MessageUtil.listToSpacedString(deaths)))
                .setColor(Color.BLUE)
                .build();
    }

    public MessageEmbed getTopBlockEmbed() {
        final EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Top 25 wykopanych i postawionych bloków")
                .setColor(Color.BLUE);

        final Map<String, Long> brokenMap = this.statsManager.getBlockBroken();
        final Map<String, Long> placedMap = this.statsManager.getBlockPlaced();

        final Map<String, Long> combinedMap = new HashMap<>(brokenMap);

        placedMap.forEach((key, value) ->
                combinedMap.merge(key, value, Long::sum)
        );

        final List<Map.Entry<String, Long>> sortedCombinedEntries = combinedMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(25)
                .toList();

        int place = 1;
        int counter = 0;
        for (final Map.Entry<String, Long> entry : sortedCombinedEntries) {
            if (counter != 25) {
                embed.addField(place + ". " + entry.getKey(), "> Wykopane: **" + brokenMap.getOrDefault(entry.getKey(), 0L) + "** \n" +
                                "> Postawione: **" + placedMap.getOrDefault(entry.getKey(), 0L) + "**",
                        true);

                counter++;
            }
            place++;
        }
        return embed.build();
    }

    private MessageEmbed getStatsEmbed() {
        this.statsButtons.clear();

        final Button enable = Button.primary("stats_enable", "Włącz").withEmoji(Emoji.fromUnicode("✅"));
        final Button disable = Button.primary("stats_disable", "Wyłącz").withEmoji(Emoji.fromUnicode("🛑"));

        if (this.serverProcess.isEnabled()) {
            this.statsButtons.add(enable.asDisabled());
            this.statsButtons.add(disable);
        } else {
            this.statsButtons.add(enable);
            this.statsButtons.add(disable.asDisabled());
        }

        return new EmbedBuilder()
                .setTitle("Statystyki ")
                .setDescription(MessageUtil.listToSpacedString(StatusUtil.getMainStats(true)))
                .setColor(Color.BLUE)
                .build();
    }

    private MessageEmbed getDifficultyEmbed() {
        final Difficulty currentDifficulty = this.bdsAutoEnable.getServerProperties().getDifficulty();
        final int currentDifficultyId = this.bdsAutoEnable.getServerProperties().getDifficulty().getDifficultyId();
        this.difficultyButtons.clear();

        final Button peaceful = Button.primary("peaceful", "Pokojowy").withEmoji(Emoji.fromUnicode("☮️"));
        final Button easy = Button.primary("easy", "Łatwy").withEmoji(Emoji.fromFormatted("<:NOOB:717474733167476766>"));
        final Button normal = Button.primary("normal", "Normalny").withEmoji(Emoji.fromFormatted("<:bao_block_grass:1019717534976577617>"));
        final Button hard = Button.primary("hard", "Trudny").withEmoji(Emoji.fromUnicode("⚠️"));

        if (currentDifficultyId != 0) {
            this.difficultyButtons.add(peaceful);
        } else {
            this.difficultyButtons.add(peaceful.asDisabled());
        }

        if (currentDifficultyId != 1) {
            this.difficultyButtons.add(easy);
        } else {
            this.difficultyButtons.add(easy.asDisabled());
        }

        if (currentDifficultyId != 2) {
            this.difficultyButtons.add(normal);
        } else {
            this.difficultyButtons.add(normal.asDisabled());
        }

        if (currentDifficultyId != 3) {
            this.difficultyButtons.add(hard);
        } else {
            this.difficultyButtons.add(hard.asDisabled());
        }

        return new EmbedBuilder()
                .setTitle("Difficulty")
                .setDescription("Aktualny poziom trudności to: " + "`" + currentDifficulty.getDifficultyName() + "`")
                .setColor(Color.BLUE)
                .build();
    }

    private MessageEmbed getBackupEmbed() {
        final String backupStatus = "`" + this.backupModule.getStatus() + "`\n";
        final long gbSpace = MathUtil.bytesToGB(StatusUtil.availableDiskSpace());

        final List<String> description = new LinkedList<>();
        this.backupButtons.clear();
        this.backupButtons.add(Button.primary("backup", "Backup")
                .withEmoji(Emoji.fromFormatted("<:bds:1138355151258783745>")));

        for (final Path path : this.backupModule.getBackups()) {
            final String fileName = path.getFileName().toString();
            if (!Files.exists(path)) continue;
            if (!(this.backupButtons.size() == 5)) {
                this.backupButtons.add(Button.danger("delete_backup:" + fileName, "Usuń " + fileName)
                        .withEmoji(Emoji.fromUnicode("🗑️")));
            }

            description.add("Nazwa: `" + fileName + "` Rozmiar: `" + this.backupModule.getBackupSize(path.toFile(), true) + "`");
        }

        return new EmbedBuilder()
                .setTitle("Backup info")
                .setDescription("Status ostatniego backup: " + backupStatus +
                        "Następny backup za: `" + DateUtil.formatTime(this.backupModule.calculateMillisUntilNextBackup(), List.of('d', 'h', 'm', 's')) + "`\n" +
                        (description.isEmpty() ? "**Brak dostępnych backup**" : "**Dostępne backupy**:\n" + MessageUtil.listToSpacedString(description) + "\n") +
                        (gbSpace < 2 ? "**Zbyt mało pamięci aby wykonać backup!**" : ""))
                .setColor(Color.BLUE)
                .build();
    }

    private MessageEmbed getServerInfoEmbed(final String address, final int port) {
        final BedrockQuery query = BedrockQuery.create(address, port);
        final EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Server info")
                .setFooter(address + ":" + port)
                .setColor(Color.BLUE);

        if (query.online()) {
            final Gamemode gamemode = query.gamemode();
            final int portV4 = query.portV4();
            final int portV6 = query.portV6();

            //TODO: Zrobić to w fieldach
            final StringBuilder description = new StringBuilder();
            description.append("**Ping:** ").append(query.responseTime()).append("\n");
            description.append("**Wersja Minecraft:** ").append(query.minecraftVersion()).append("\n");
            description.append("**Protokół:** ").append(query.protocol()).append("\n");
            description.append("**MOTD:** ").append(query.motd()).append("\n");
            description.append("**Nazwa Mapy:** ").append(query.mapName()).append("\n");
            description.append("**Gracz online:** ").append(query.playerCount()).append("\n");
            description.append("**Maksymalna ilość graczy:** ").append(query.maxPlayers()).append("\n");
            description.append("**Tryb Gry:** ").append(gamemode.getName().toUpperCase()).append(" (").append(gamemode.getId()).append(")").append("\n");
            description.append("**Edycja:** ").append(query.edition()).append("\n");

            if (portV4 != -1) {
                description.append("**Port v4:** ").append(portV4).append("\n");
            }
            if (portV6 != -1) {
                description.append("**Port v6:** ").append(portV6).append("\n");
            }

            embedBuilder.setDescription(description.toString());

        } else {
            embedBuilder.setDescription("Nie można uzyskać informacji o serwerze ``" + address + ":" + port + "``");
        }

        return embedBuilder.build();
    }
}
