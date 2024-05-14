package me.indian.js;

import java.util.List;

public record ScriptDescription(String main, String version, String name,String engineName,
                                String description, String author, List<String> authors) {
}
