package me.indian.js;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import me.indian.bds.BDSAutoEnable;
import me.indian.bds.logger.Logger;
import me.indian.bds.logger.impl.ExtensionLogger;
import me.indian.bds.util.GsonUtil;
import me.indian.bds.util.MessageUtil;
import org.jetbrains.annotations.Nullable;

public class ScriptManager {

    private final ScriptExtension scriptExtension;
    private final BDSAutoEnable bdsAutoEnable;
    private final Logger logger;
    private final ScriptEngineManager manager;
    private final Map<String, Script> scriptMap;
    private final File[] scriptFiles;
    private final Map<String, Logger> engineLoggers;

    public ScriptManager(final ScriptExtension scriptExtension) {
        this.scriptExtension = scriptExtension;
        this.bdsAutoEnable = scriptExtension.getBdsAutoEnable();
        this.logger = this.scriptExtension.getLogger();
        this.manager = new ScriptEngineManager();
        this.scriptMap = new HashMap<>();
        this.scriptFiles = new File(this.scriptsPath()).listFiles(File::isDirectory);
        this.engineLoggers = new HashMap<>();

        if (scriptExtension.getConfig().isPrintEngines()) this.printEngines(this.manager);
    }

    private ScriptEngine getEngineByName(final String engineName) {
        final List<ScriptEngineFactory> availableEngines = this.manager.getEngineFactories();
        ScriptEngine scriptEngine = this.manager.getEngineByName(engineName);

        if (scriptEngine == null && !availableEngines.isEmpty()) {
            final ScriptEngineFactory newEngineFactory = availableEngines.get(0);
            scriptEngine = this.manager.getEngineByName(newEngineFactory.getLanguageName());

            if (scriptEngine != null) {
                this.logger.info("&aNie udało się odnależć silnika o nazwie&b " + engineName + "&a ale znaleźliśmy&1 "
                        + newEngineFactory.getEngineName() + " " + newEngineFactory.getEngineVersion() + " &d(&e" + newEngineFactory.getLanguageName() + "&d)");
            }
        }

        if (scriptEngine == null) {
            throw new NullPointerException("Nie znaleziono silnika o nazwie " + engineName);
        }

        this.putVariables(scriptEngine);

        return scriptEngine;
    }

    private void putVariables(final ScriptEngine scriptEngine) {
        scriptEngine.put("bds", this.bdsAutoEnable);
        scriptEngine.put("server", this.bdsAutoEnable.getServerProcess());
        scriptEngine.put("console", this.getEngineLogger(scriptEngine));
        scriptEngine.put("logger", this.getEngineLogger(scriptEngine));
        scriptEngine.put("properties", this.bdsAutoEnable.getServerProperties());
        scriptEngine.put("serverManager", this.bdsAutoEnable.getServerManager());
        scriptEngine.put("versionManager", this.bdsAutoEnable.getVersionManager());
        scriptEngine.put("eventManager", this.bdsAutoEnable.getEventManager());
        scriptEngine.put("extensionManager", this.bdsAutoEnable.getExtensionManager());
        scriptEngine.put("allowlistManager", this.bdsAutoEnable.getAllowlistManager());
        scriptEngine.put("packManager", this.bdsAutoEnable.getPackManager());
        scriptEngine.put("commandManager", this.bdsAutoEnable.getCommandManager());
        scriptEngine.put("watchDog", this.bdsAutoEnable.getWatchDog());
    }

    private Logger getEngineLogger(final ScriptEngine scriptEngine) {
        final String name = scriptEngine.getFactory().getLanguageVersion();

        if (this.engineLoggers.containsKey(name)) return this.engineLoggers.get(name);
        final Logger engineLogger = new ExtensionLogger(this.bdsAutoEnable, name);

        this.engineLoggers.put(name, engineLogger);

        return engineLogger;
    }

    private void printEngines(final ScriptEngineManager scriptEngineManager) {
        if (scriptEngineManager.getEngineFactories().isEmpty()) {
            this.logger.error("&cBrak silników&b JavaScript");
            return;
        }

        this.logger.print();
        for (final ScriptEngineFactory factory : scriptEngineManager.getEngineFactories()) {
            this.logger.print();
            this.logger.info("Nazwa: " + factory.getEngineName());
            this.logger.info("Wersja: " + factory.getEngineVersion());
            this.logger.info("Nazwa języka: " + factory.getLanguageName());
            this.logger.info("Wersja języka: " + factory.getLanguageVersion());
            this.logger.info("Rozszerzenia: " + MessageUtil.stringListToString(factory.getExtensions(), ", "));
            this.logger.info("Nazwy: " + MessageUtil.stringListToString(factory.getNames(), ", "));
            this.logger.info("Typy MIME: " + MessageUtil.stringListToString(factory.getMimeTypes(), ", "));
            this.logger.info("-------");
        }
        this.logger.print();
    }

    public Script loadScript(final File file) {
        final Path scriptPath = Path.of(file.getPath());
        final ScriptDescription scriptDescription = this.loadScriptDescription(scriptPath);

        if (scriptDescription == null) {
            this.logger.critical("(&2" + file.getName() + "&r) Plik &bScript.json&r ma nieprawidłową składnie albo nie istnieje");
            return null;
        }

        final Script script = new Script(scriptPath, this.getEngineByName(scriptDescription.engineName()), scriptDescription);
        this.scriptMap.put(scriptDescription.name(), script);
        return script;
    }

    public void invokeAllScripts() {
        for (final Script script : this.scriptMap.values()) {
            try {
                this.invokeScript(script);
            } catch (final IOException | ScriptException exception) {
                this.logger.error("&cNie udało się wykonać skryptu&b " + script.getScriptDescription().name(), exception);
            }
        }
    }

    public void invokeScript(final Script script) throws IOException, ScriptException {
        //TODO: Jak to gówno bedzie przydatne dodaj Wielowątkowość
        final ScriptDescription scriptDescription = script.getScriptDescription();
        final String scriptMain = scriptDescription.main();
        final File index = new File(script.getScriptPath() + File.separator + scriptMain);

        try (final FileReader reader = new FileReader(index)) {
            script.getScriptEngine().eval(reader);
        }

        this.logger.info("Włączono&b " + scriptDescription.name() + "&r (Wersja:&a " + scriptDescription.version()
                + "&r Autor:&a " + scriptDescription.author() + "&r Silnik:&1 " + script.getScriptEngine().getFactory().getLanguageName() + "&r)");
    }

    public void loadScripts() {
        if (this.scriptFiles == null) return;

        for (final File file : this.scriptFiles) {
            this.loadScript(file);
        }
    }

    @Nullable
    public ScriptDescription loadScriptDescription(final Path scriptPath) {
        final File file = new File(scriptPath.toString(), "Script.json");

        try (final FileReader reader = new FileReader(file)) {
            final ScriptDescription description = GsonUtil.getGson().fromJson(reader, ScriptDescription.class);

            final String author = description.author();
            List<String> authors = description.authors();

            if (authors == null) authors = new ArrayList<>();
            if (authors.isEmpty() || !authors.contains(author)) authors.add(author);

            return new ScriptDescription(description.main(), description.version(), description.name(),
                    description.engineName(), description.description(), description.author(), authors);
        } catch (final Exception exception) {
            return null;
        }
    }

    public String scriptsPath() {
        final String path = this.scriptExtension.getDataFolder() + File.separator + "scripts";

        try {
            Files.createDirectories(Path.of(path));
        } catch (final IOException ioException) {
            throw new RuntimeException(ioException);
        }

        return path;
    }
}