package pl.indianbartonka.js;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import pl.indianbartonka.util.MessageUtil;

public class Test {


    public static void main(String[] args) {

        final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

        if (scriptEngineManager.getEngineFactories().isEmpty()) {
            System.out.println("&cBrak silników&b JavaScript");
            return;
        }

        System.out.println();
        for (final ScriptEngineFactory factory : scriptEngineManager.getEngineFactories()) {
            System.out.println();
            System.out.println("Nazwa: " + factory.getEngineName());
            System.out.println("Wersja: " + factory.getEngineVersion());
            System.out.println("Nazwa języka: " + factory.getLanguageName());
            System.out.println("Wersja języka: " + factory.getLanguageVersion());
            System.out.println("Rozszerzenia: " + MessageUtil.stringListToString(factory.getExtensions(), ", "));
            System.out.println("Nazwy: " + MessageUtil.stringListToString(factory.getNames(), ", "));
            System.out.println("Typy MIME: " + MessageUtil.stringListToString(factory.getMimeTypes(), ", "));
            System.out.println("-------");
        }
        System.out.println();


        ScriptEngine engine = scriptEngineManager.getEngineByName("js");

        if (engine == null) {
            System.out.println("JavaScript engine not found.");
            return;
        }

        try {
            engine.eval("print('Hello, JavaScript!')");
        } catch (ScriptException e) {
            System.out.println("Error evaluating JavaScript code: " + e.getMessage());
        }


    }

}
