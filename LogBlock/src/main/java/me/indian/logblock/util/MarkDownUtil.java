package me.indian.logblock.util;

public final class MarkDownUtil {

    public static String formatInfo(final String date, final String json) {
        return """
                # DATE
                                
                ```json
                JSON
                ```
                """.replaceAll("DATE", date).replaceAll("JSON", json);
    }
}