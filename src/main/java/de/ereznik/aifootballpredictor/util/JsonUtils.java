package de.ereznik.aifootballpredictor.util;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;

import java.util.List;

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

    /**
     * Builds the JSON structure template with actual team names from the matches.
     * Prediction fields are left empty for the AI model to fill in.
     */
    public static String buildJsonStructure(List<MatchesResponse.Match> matches) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"matches\": [\n");

        for (int i = 0; i < matches.size(); i++) {
            MatchesResponse.Match match = matches.get(i);
            json.append("    {\n");
            json.append("      \"home\": \"").append(match.homeTeam().name()).append("\",\n");
            json.append("      \"away\": \"").append(match.awayTeam().name()).append("\",\n");
            json.append("      \"homeId\": ").append(match.homeTeam().id()).append(",\n");
            json.append("      \"awayId\": ").append(match.awayTeam().id()).append(",\n");
            json.append("      \"predictedHome\": ,\n");
            json.append("      \"predictedAway\": ,\n");
            json.append("      \"confidence\": \n");
            json.append("    }");
            if (i < matches.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}\n");

        return json.toString();
    }
}