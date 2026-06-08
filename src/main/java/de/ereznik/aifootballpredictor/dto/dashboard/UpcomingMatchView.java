package de.ereznik.aifootballpredictor.dto.dashboard;

import java.util.Map;

public record UpcomingMatchView(
        String competition,
        Integer gameDay,
        String homeTeam,
        String awayTeam,
        ConsensusPredictionView consensusPrediction,
        Map<String, String> predictionsByModel,
        Map<String, Integer> probabilitiesByModel
) {
}
