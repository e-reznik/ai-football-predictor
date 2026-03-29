package de.ereznik.aifootballpredictor.util;

public class JsonUtils {
    private JsonUtils() {
    }

    public static String cleanJson(String json) {
        String cleaned = json.strip();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("```\\s*$", "").strip();
        }
        return cleaned;
    }
}
