package me.indian.extension.discord.config.sub;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class LinkingConfig extends OkaeriConfig {


    @Comment({""})
    @Comment({"ID roli którą bedzie otrzymywał użytkownik po połączeniu kont"})
    private long linkedRoleID = 1L;

    @Comment({""})
    @Comment({"ID roli którą bedzie otrzymywał użytkownik po połączeniu kont jeśli ma sie 5h czasu gry na serwerze"})
    private long linkedPlaytimeRoleID = 1L;

    @Comment({""})
    @Comment({"Czy użytkownik może pisać na kanale bez połączonych kont?"})
    private boolean canType = false;

    @Comment({""})
    @Comment({"Wiadomość która zostanie wysłana na pv do użytkownika bez połączonych kont"})
    private String cantTypeMessage = "Aby wysyłać wiadomości na tym kanale musisz mieć połączone konta Discord i Minecraft ";

    @Comment({""})
    @Comment({"Czy użytkownik może sam rozłączyć swoje konto Discord z kontem Minecraft?"})
    private boolean canUnlink = false;

    public long getLinkedRoleID() {
        return this.linkedRoleID;
    }

    public long getLinkedPlaytimeRoleID() {
        return this.linkedPlaytimeRoleID;
    }

    public boolean isCanType() {
        return this.canType;
    }

    public String getCantTypeMessage() {
        return this.cantTypeMessage;
    }

    public boolean isCanUnlink() {
        return this.canUnlink;
    }
}