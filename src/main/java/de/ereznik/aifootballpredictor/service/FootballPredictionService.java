package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.client.FootballClient;
import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;
import de.ereznik.aifootballpredictor.dto.football.Competition;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
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
    private final AIService aiService;
    private final PredictionPersistenceService predictionPersistenceService;
    private final List<Competition> competitions;
    private final List<ChatModel> chatModels;

    public FootballPredictionService(FootballClient footballClient, AIService aiService, PredictionPersistenceService predictionPersistenceService, @Value("${football-data.competitions}") List<Competition> competitions, List<ChatModel> chatModels) {
        this.footballClient = footballClient;
        this.aiService = aiService;
        this.predictionPersistenceService = predictionPersistenceService;
        this.competitions = competitions;
        this.chatModels = chatModels;

        log.info("Loaded Competitions: {}", competitions.stream()
                .map(m -> m.toString())
                .toList());
        log.info("Loaded ChatModels: {}", chatModels.stream()
                .map(m -> m.getClass().getSimpleName() + "(" + m.getDefaultOptions().getModel() + ")")
                .toList());
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Europe/Berlin")
    public void runPredictions() {
        List<MatchesResponse> matches = getMatches();

        Map<Competition, String> prompts = aiService.createPrompts(matches);
        List<PredictionResponse> predictions = aiService.getAnswerFromChatModel(chatModels, prompts);

        predictionPersistenceService.persist(matches, predictions);
    }

    public List<MatchesResponse> getMatches() {
        List<MatchesResponse> matchesAllLeagues = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Competition competition : competitions) {
            log.info("Getting matches for {} for {}", competition, today);
            MatchesResponse matchesOneLeague = footballClient.fetchScheduledMatches(competition, today, today);
            if (matchesOneLeague != null && !matchesOneLeague.matches().isEmpty()) {
                matchesAllLeagues.add(matchesOneLeague);
            } else {
                // TODO: Handle cases, when no matches are played
                log.warn("No matches found for {}", competition);
            }
        }
        return matchesAllLeagues;
    }
}