package me.indian.example;

import me.indian.bds.BDSAutoEnable;
import me.indian.bds.command.CommandManager;
import me.indian.bds.event.EventManager;
import me.indian.bds.extension.Extension;
import me.indian.bds.logger.Logger;
import me.indian.bds.util.BedrockQuery;
import me.indian.example.command.ExampleCommand;
import me.indian.example.config.Config;
import me.indian.example.listener.ServerListener;

public class ExampleExtension extends Extension {

    private Config config;

    @Override
    public void onEnable() {

        final BDSAutoEnable bdsAutoEnable = this.getBdsAutoEnable();
        final Logger logger = this.getBdsAutoEnable().getLogger();


        //TODO: Dodać przykład jak pingowac inny server

        //Tworzenie poleceń
        final CommandManager commandManager = bdsAutoEnable.getCommandManager();

        //Przekazujemy w konstruktorze `this` czyli aktualną klase
        commandManager.registerCommand(new ExampleCommand(this));

        //Tworzenie configu
        try {
            this.config = this.createConfig(Config.class, "config");
        } catch (final Exception exception) {
            logger.error("Nie można utworzyć configu", exception);
        }


        //Tworzenie mało zawansowanych listenerów
        final EventManager eventManager = bdsAutoEnable.getEventManager();
        eventManager.registerListener(new ServerListener());

        //Wysyłanie komend do konsoli servera z wiadomoscią z config
        bdsAutoEnable.getServerProcess().sendToConsole("say " + this.config.getSubConfig().getCos());

        //Robienie backup
        bdsAutoEnable.getWatchDog().getBackupModule().backup();

        //Pozyskiwanie wartości z Server.Properties
        //Surowe
        logger.info(bdsAutoEnable.getServerProperties().getProperties().getProperty("server-port"));
        //Już dostosowane
        logger.info(bdsAutoEnable.getServerProperties().getServerPort());
        //W przypadku zmiany jakiejś wartości użyj 'set', wymaga restartu servera!


        //Pozyskiwanie wartości
        logger.info(this.getExtensionDescription().name());
        logger.info(this.getExtensionDescription().description());
        logger.info(this.getName());
        logger.info(this.getDescription());
        logger.info(this.getAuthor());

        //Możesz także z łatwoscią pozyskać informacie z guery innego servera bedrock
        final BedrockQuery query = BedrockQuery.create("play.skyblockpe.com", 19132);

        if(query.online()) {
            logger.info("&aMOTD:&b " + query.motd());
            logger.info("&aProtocol Version:&b " + query.protocol());
            logger.info("&aMinecraft Version:&b " + query.minecraftVersion());
            logger.info("&aPlayer Count:&b " + query.playerCount());
            logger.info("&aMax Players:&b " + query.maxPlayers());
            logger.info("&aMap Name:&b " + query.mapName());
            logger.info("&aGamemode:&b " + query.gamemode());
            logger.info("&aEdycja:&b " + query.edition());
        }

        //Wysyłanie wiadomości do konsoli aplikacji
        logger.info("Włączono ROZSERZENIE");
    }

    @Override
    public void onDisable() {
        //Akcje wykonywane przy zamykaniu aplikacji
    }

    public Config getConfig() {
        return this.config;
    }
}
