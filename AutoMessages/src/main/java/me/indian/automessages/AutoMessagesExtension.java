package me.indian.automessages;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import me.indian.automessages.config.AutoMessagesConfig;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.DateUtil;
import me.indian.bds.util.ServerUtil;

public class AutoMessagesExtension extends Extension {

    private final Timer timer = new Timer("AutoMessages", true);
    private TimerTask autoMessagesTask;

    @Override
    public void onEnable() {
        final BDSAutoEnable bdsAutoEnable = this.getBdsAutoEnable();
        final AutoMessagesConfig autoMessagesConfig = this.createConfig(AutoMessagesConfig.class, "config");
        final ServerProcess serverProcess = bdsAutoEnable.getServerProcess();
        final Random random = new Random();
        final List<String> messages = autoMessagesConfig.getMessages();

        this.autoMessagesTask = new TimerTask() {
            Iterator<String> iterator = messages.iterator();

            @Override
            public void run() {
                if (serverProcess.isEnabled() && !bdsAutoEnable.getServerManager().getOnlinePlayers().isEmpty()) {
                    if (!this.iterator.hasNext()) this.iterator = messages.iterator();
                    final String prefix = autoMessagesConfig.getPrefix();

                    if (autoMessagesConfig.isRandom()) {
                        final int message = random.nextInt(messages.size());

                        ServerUtil.tellrawToAll(prefix + messages.get(message));
                    } else {
                        ServerUtil.tellrawToAll(prefix + this.iterator.next());
                    }
                }
            }
        };
        if (autoMessagesConfig.isEnabled()) {
            this.timer.scheduleAtFixedRate(this.autoMessagesTask, 0, DateUtil.secondToMillis(autoMessagesConfig.getTime()));
        } else {
            this.getLogger().debug("&aAutomessages jest&c wyłączone");
        }
    }

    @Override
    public void onDisable() {
        if (this.autoMessagesTask != null) {
            this.autoMessagesTask.cancel();
            this.timer.cancel();
        }
    }
}