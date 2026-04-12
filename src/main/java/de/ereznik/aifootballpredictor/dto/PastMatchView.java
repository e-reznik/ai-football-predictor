package de.ereznik.aifootballpredictor.dto;

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
) {}