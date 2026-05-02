package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.client.FootballClient;
import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;
import de.ereznik.aifootballpredictor.dto.football.Competition;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.news.NewsPerMatchPerCompetition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FootballPredictionService {
    private final FootballClient footballClient;
    private final LatestNewsService latestNewsService;
    private final AIService aiService;
    private final PredictionPersistenceService predictionPersistenceService;
    private final RandomPredictionService randomPredictionService;

    public FootballPredictionService(FootballClient footballClient, LatestNewsService latestNewsService, AIService aiService, PredictionPersistenceService predictionPersistenceService, @Value("${football-data.competitions}") List<Competition> competitions, RandomPredictionService randomPredictionService) {
        this.footballClient = footballClient;
        this.latestNewsService = latestNewsService;
        this.aiService = aiService;
        this.predictionPersistenceService = predictionPersistenceService;
        this.randomPredictionService = randomPredictionService;

        log.info("Loaded Competitions: {}", competitions.stream()
                .map(m -> m.toString())
                .toList());
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Europe/Berlin")
    public void runPredictions() {
        List<MatchesResponse> matches = getMatches();
        NewsPerMatchPerCompetition newsPerMatchPerCompetition = latestNewsService.getNews(matches);
        Map<Competition, String> promptsPerCompetition = aiService.createPrompts(newsPerMatchPerCompetition);
        List<PredictionResponse> predictions = aiService.getAnswerFromChatModel(promptsPerCompetition);
        predictions.addAll(randomPredictionService.predict(matches));
        predictionPersistenceService.persist(matches, predictions);
    }

    public List<MatchesResponse> getMatches() {
        List<MatchesResponse> matchesAllLeagues = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Getting matches for {} - {}", today, tomorrow);
        MatchesResponse matchesOneLeague = footballClient.fetchScheduledMatches(today, tomorrow);
        if (matchesOneLeague != null && !matchesOneLeague.matches().isEmpty()) {
            matchesAllLeagues.add(matchesOneLeague);
        } else {
            // TODO: Handle cases, when no matches are found
            log.info("No matches found for {}", today);
        }
        return matchesAllLeagues;
    }
}