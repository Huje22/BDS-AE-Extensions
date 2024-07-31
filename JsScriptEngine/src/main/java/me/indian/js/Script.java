package me.indian.js;

import java.nio.file.Path;
import javax.script.ScriptEngine;


public class Script {

    private final Path scriptPath;
    private final ScriptEngine scriptEngine;
    private final ScriptDescription scriptDescription;


    public Script(final Path scriptPath, final ScriptEngine scriptEngine, final ScriptDescription scriptDescription) {
        this.scriptPath = scriptPath;
        this.scriptEngine = scriptEngine;
        this.scriptDescription = scriptDescription;
    }


    public Path getScriptPath() {
        return this.scriptPath;
    }

    public ScriptEngine getScriptEngine() {
        return this.scriptEngine;
    }

    public ScriptDescription getScriptDescription() {
        return this.scriptDescription;
    }
}
