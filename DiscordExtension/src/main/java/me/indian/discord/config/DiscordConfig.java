package me.indian.discord.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;
import me.indian.discord.config.sub.BotConfig;
import me.indian.discord.config.sub.WebHookConfig;

@Header("################################################################")
@Header("#           Ustawienia Integracji z Discord                    #")
@Header("################################################################")

public class DiscordConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Ustawienia webhooka"})
    @CustomKey("WebHook")
    private WebHookConfig webHookConfig = new WebHookConfig();
    @Comment({""})
    @Comment({"Ustawienia Bota"})
    @CustomKey("Bot")
    private BotConfig botConfig = new BotConfig();

    public WebHookConfig getWebHookConfig() {
        return this.webHookConfig;
    }

    public BotConfig getBotConfig() {
        return this.botConfig;
    }

}