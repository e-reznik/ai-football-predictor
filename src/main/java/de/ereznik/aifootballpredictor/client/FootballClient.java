package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.Competition;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;

import java.time.LocalDate;

public interface FootballClient {
    MatchesResponse fetchScheduledMatches(Competition competition, LocalDate from, LocalDate to);

    MatchesResponse fetchFinishedMatches(Competition competition, LocalDate from, LocalDate to);

}
