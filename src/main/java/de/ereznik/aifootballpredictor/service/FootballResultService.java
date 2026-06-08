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
        LocalDate to = LocalDate.now().plusDays(5);

        log.info("Getting match status updates from {} to {}", from, to);
        MatchesResponse matchesOneLeague = footballClient.fetchMatchUpdates(from, to);
        if (matchesOneLeague != null && !matchesOneLeague.matches().isEmpty()) {
            matchesAllLeagues.add(matchesOneLeague);
        } else {
            log.warn("No match status updates found from {} to {}", from, to);
        }
        return matchesAllLeagues;
    }
}
