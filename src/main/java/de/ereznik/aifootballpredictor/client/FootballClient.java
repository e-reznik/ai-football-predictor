package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;

import java.time.LocalDate;

public interface FootballClient {
    MatchesResponse fetchScheduledMatches(LocalDate from, LocalDate to);

    MatchesResponse fetchFinishedMatches(LocalDate from, LocalDate to);

}
