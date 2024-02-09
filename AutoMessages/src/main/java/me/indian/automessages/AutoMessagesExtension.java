package me.indian.automessages;

import me.indian.automessages.config.AutoMessagesConfig;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.extension.Extension;
import me.indian.bds.server.ServerProcess;
import me.indian.bds.util.MathUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class AutoMessagesExtension extends Extension {

    @Override
    public void onEnable() {
        final BDSAutoEnable bdsAutoEnable = this.getBdsAutoEnable();
        final AutoMessagesConfig autoMessagesConfig = this.createConfig(AutoMessagesConfig.class, "config");
        final ServerProcess serverProcess = bdsAutoEnable.getServerProcess();
        final Timer timer = new Timer("AutoMessages", true);
        final Random random = new Random();
        final List<String> messages = autoMessagesConfig.getMessages();


        final TimerTask autoMessages = new TimerTask() {
            Iterator<String> iterator = messages.iterator();

            @Override
            public void run() {
                if (serverProcess.isEnabled() && !bdsAutoEnable.getServerManager().getOnlinePlayers().isEmpty()) {
                    if (!this.iterator.hasNext()) this.iterator = messages.iterator();
                    final String prefix = autoMessagesConfig.getPrefix();

                    if (autoMessagesConfig.isRandom()) {
                        final int message = random.nextInt(messages.size());

                        serverProcess.tellrawToAll(prefix + messages.get(message));
                    } else {
                        serverProcess.tellrawToAll(prefix + this.iterator.next());
                    }
                }
            }
        };
        if (autoMessagesConfig.isEnabled()) {
            timer.scheduleAtFixedRate(autoMessages, 0, MathUtil.secondToMillis(autoMessagesConfig.getTime()));
        } else {
             this.getLogger().debug("&aAutomessages jest&c wyłączone");
        }
    }
}