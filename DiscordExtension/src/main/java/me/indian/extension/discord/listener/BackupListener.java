package me.indian.extension.discord.listener;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.event.Listener;
import me.indian.bds.event.watchdog.BackupDoneEvent;
import me.indian.bds.event.watchdog.BackupFailEvent;
import me.indian.extension.discord.DiscordExtension;

public class BackupListener extends Listener {

    private final DiscordExtension discordExtension;
    private final BDSAutoEnable bdsAutoEnable;

    public BackupListener(final DiscordExtension discordExtension){
        this.discordExtension =  discordExtension;
        this.bdsAutoEnable = this.discordExtension.getBdsAutoEnable();
    }

    @Override
    public void onBackupDone(final BackupDoneEvent event){
        this.discordExtension.getDiscordJDA().sendBackupDoneMessage();

    }


    @Override
    public void onBackupFail(final BackupFailEvent event){
        this.discordExtension.getDiscordJDA().sendBackupFailMessage(event.getException());
    }
}
