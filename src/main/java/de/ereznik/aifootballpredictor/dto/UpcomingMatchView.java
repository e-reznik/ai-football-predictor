package de.ereznik.aifootballpredictor.dto;

import java.util.Map;

public record UpcomingMatchView(
        String competition,
        int gameDay,
        String homeTeam,
        String awayTeam,
        Map<String, String> predictionsByModel
) {}
