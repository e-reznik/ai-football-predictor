package de.ereznik.aifootballpredictor.dto.dashboard;

import java.util.Map;

public record PastMatchView(
        String competition,
        int gameDay,
        String homeTeam,
        String awayTeam,
        int homeGoalsScored,
        int awayGoalsScored,
        Map<String, String> predictionsByModel,
        Map<String, Integer> scoresByModel
) {
    public int correctPredictions() {
        return (int) scoresByModel.values().stream().filter(s -> s > 0).count();
    }

    public int totalScoredPredictions() {
        return scoresByModel.size();
    }
}