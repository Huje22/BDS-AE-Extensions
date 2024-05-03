package me.indian.discord.jda.command.defaults;

import java.awt.Color;
import me.indian.discord.DiscordExtension;
import me.indian.discord.jda.command.SlashCommand;
import me.indian.discord.jda.manager.LinkingManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class UnLinkCommand implements SlashCommand {

    private final LinkingManager linkingManager;

    public UnLinkCommand(final DiscordExtension discordExtension) {
        this.linkingManager = discordExtension.getDiscordJDA().getLinkingManager();
    }

    @Override
    public void onExecute(final SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;


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

    @Override
    public SlashCommandData getCommand() {
        return Commands.slash("unlink", "Rozłącza konto Discord z kontem nickiem Minecraft")
                .addOption(OptionType.STRING, "name", "Nick użytkownika którego konto ma zostać rozłączone", false);
    }
}