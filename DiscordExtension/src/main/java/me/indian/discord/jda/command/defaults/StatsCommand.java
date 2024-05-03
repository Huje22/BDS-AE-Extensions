package me.indian.discord.jda.command.defaults;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MessageUtil;
import me.indian.bds.util.ServerUtil;
import me.indian.bds.util.StatusUtil;
import me.indian.bds.util.ThreadUtil;
import me.indian.discord.DiscordExtension;
import me.indian.discord.embed.component.Footer;
import me.indian.discord.jda.DiscordJDA;
import me.indian.discord.jda.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class StatsCommand extends ListenerAdapter implements SlashCommand {

    private final DiscordJDA discordJDA;
    private final ServerProcess serverProcess;
    private final List<Button> statsButtons;

    public StatsCommand(final DiscordExtension discordExtension) {
        this.discordJDA = discordExtension.getDiscordJDA();
        this.serverProcess = discordExtension.getBdsAutoEnable().getServerProcess();
        this.statsButtons = new LinkedList<>();
    }

    @Override
    public void onExecute(final SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;


        if (member.hasPermission(Permission.MANAGE_SERVER)) {
            event.getHook().editOriginalEmbeds(this.getStatsEmbed())
                    .setActionRow(ActionRow.of(this.statsButtons).getComponents())
                    .queue();
        } else {
            event.getHook().editOriginalEmbeds(this.getStatsEmbed()).queue();
        }
    }

    @Override
    public void onButtonInteraction(final ButtonInteractionEvent event) {
        if (!event.getComponentId().contains("stats_")) return;
        final Member member = event.getMember();
        if (member == null) return;

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
                ServerUtil.kickAllPlayers("&aServer został wyłączony za pośrednictwem&b discord");
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

    @Override
    public SlashCommandData getCommand() {
        return Commands.slash("stats", "Statystyki Servera i aplikacji.");
    }
}