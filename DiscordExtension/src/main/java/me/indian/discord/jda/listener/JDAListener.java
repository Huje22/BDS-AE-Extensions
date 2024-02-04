package me.indian.discord.jda.listener;

import me.indian.bds.server.ServerProcess;

public interface JDAListener {

    void init();

    void initServerProcess(final ServerProcess serverProcess);

}
