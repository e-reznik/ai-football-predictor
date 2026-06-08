package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;

import java.time.LocalDate;

public interface FootballClient {
    MatchesResponse fetchScheduledMatches(LocalDate from, LocalDate to);

    MatchesResponse fetchMatchUpdates(LocalDate from, LocalDate to);
}
