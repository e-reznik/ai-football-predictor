package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.client.FootballClient;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.ml.PredictionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FootballService {
    private final FootballClient footballClient;
    private final AIService aiService;
    private final PredictionPersistenceService predictionPersistenceService;
    private final List<String> competitions;
    private final List<ChatModel> chatModels;

    public FootballService(FootballClient footballClient, AIService aiService, PredictionPersistenceService predictionPersistenceService, @Value("${football-data.competitions}") List<String> competitions, List<ChatModel> chatModels) {
        this.footballClient = footballClient;
        this.aiService = aiService;
        this.predictionPersistenceService = predictionPersistenceService;
        this.competitions = competitions;
        this.chatModels = chatModels;

        log.info("Loaded ChatModels: {}", chatModels.stream()
                .map(m -> m.getClass().getSimpleName() + "(" + m.getDefaultOptions().getModel() + ")")
                .toList());
    }

    public Map<String, PredictionResponse> runPredictions() {
        List<MatchesResponse> matches = getMatches();
        if (matches == null || matches.isEmpty()) {
            return new HashMap<>();
        }
        List<String> prompts = aiService.createPrompts(matches);
        Map<String, PredictionResponse> predictions = aiService.getAnswerFromChatModel(chatModels, prompts);

        predictionPersistenceService.persist(matches, predictions);

        return predictions;
    }

    public List<MatchesResponse> getMatches() {
        List<MatchesResponse> matchesAllLeagues = new ArrayList<>();
        LocalDate today = LocalDate.now();
        //LocalDate today = LocalDate.of(2026, 04, 05);

        for (String competition : competitions) {
            log.info("Getting matches for {} for {}", competition, today);
            MatchesResponse matchesOneLeague = footballClient.fetchMatches(today, competition);
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