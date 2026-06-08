package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.client.FootballClient;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FootballResultService {
    private final ResultPersistenceService resultPersistenceService;
    private final FootballClient footballClient;

    public FootballResultService(ResultPersistenceService resultPersistenceService, FootballClient footballClient) {
        this.resultPersistenceService = resultPersistenceService;
        this.footballClient = footballClient;
    }

    @Scheduled(cron = "0 0 2 * * *", zone = "Europe/Berlin")
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
        LocalDate from = LocalDate.now().minusDays(3);
        LocalDate to = LocalDate.now();

        log.info("Getting finished matches from {} to {}", from, to);
        MatchesResponse matchesOneLeague = footballClient.fetchFinishedMatches(from, to);
        if (matchesOneLeague != null && !matchesOneLeague.matches().isEmpty()) {
            matchesAllLeagues.add(matchesOneLeague);
        } else {
            // TODO: Handle cases, when no matches are played
            // TODO: Handle cases, when matches got canceled or postponed
            log.warn("No finished matches found from {} to {}", from, to);
        }
        return matchesAllLeagues;
    }
}
