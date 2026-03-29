package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;

import java.time.LocalDate;

public interface FootballClient {
    MatchesResponse fetchMatches(LocalDate today, String competition);
}
