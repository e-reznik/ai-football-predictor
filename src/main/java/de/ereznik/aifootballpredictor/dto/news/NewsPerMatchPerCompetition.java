package de.ereznik.aifootballpredictor.dto.news;

import de.ereznik.aifootballpredictor.dto.football.Competition;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;

import java.util.Map;

public record NewsPerMatchPerCompetition(
        Map<Competition, Map<MatchesResponse.Match, NewsSearchResponse>> newsPerMatchPerCompetition
) {
}