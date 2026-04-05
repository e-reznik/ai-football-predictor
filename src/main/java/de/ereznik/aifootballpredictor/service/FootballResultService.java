package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.client.FootballClient;
import de.ereznik.aifootballpredictor.dto.football.Competition;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FootballResultService {
    private final ResultPersistenceService resultPersistenceService;
    private final FootballClient footballClient;
    private final List<Competition> competitions;

    public FootballResultService(ResultPersistenceService resultPersistenceService, FootballClient footballClient, @Value("${football-data.competitions}") List<Competition> competitions) {
        this.resultPersistenceService = resultPersistenceService;
        this.footballClient = footballClient;
        this.competitions = competitions;
    }

    //@Scheduled(initialDelay = 2000, fixedRate = 60000)
    public void runResults() {
        List<MatchesResponse> matches = getMatches();
        if (matches == null || matches.isEmpty()) {
            log.warn("No matches found");
            return;
        }

        resultPersistenceService.persist(matches);
    }

    public List<MatchesResponse> getMatches() {
        List<MatchesResponse> matchesAllLeagues = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Competition competition : competitions) {
            log.info("Getting matches for {} for {}", competition, today);
            MatchesResponse matchesOneLeague = footballClient.fetchFinishedMatches(competition, today, today);
            if (matchesOneLeague != null && !matchesOneLeague.matches().isEmpty()) {
                matchesAllLeagues.add(matchesOneLeague);
            } else {
                // TODO: Handle cases, when no matches are played
                // TODO: Handle cases, when matches got canceled or postponed
                log.warn("No matches found for {}", competition);
            }
        }
        return matchesAllLeagues;
    }
}
