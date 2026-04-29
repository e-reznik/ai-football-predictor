package de.ereznik.aifootballpredictor.dto.dashboard;

import java.util.List;
import java.util.Map;

public record DashboardData(
        List<String> competitions,
        List<String> models,
        Map<String, Map<String, Integer>> totalByCompetitionAndModel,
        Map<String, Map<Integer, Map<String, Integer>>> cumulativeByCompetitionAndMatchday,
        Map<String, Map<String, Integer>> accuracyByModel,
        Map<String, Integer> predictionCountByModel,
        String trackingSince,
        long totalGames,
        String lastPredictionRun,
        String lastResultsFetched
) {
}