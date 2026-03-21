package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.ml.PredictionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FootballService {
    private final RestClient restClient;
    private final AIService aiService;
    private final List<String> competitions;
    private final List<ChatModel> chatModels;

    public FootballService(RestClient restClient, AIService aiService, @Value("${football-data.competitions}") List<String> competitions, List<ChatModel> chatModels) {
        this.restClient = restClient;
        this.aiService = aiService;
        this.competitions = competitions;
        this.chatModels = chatModels;
    }

    public Map<String, PredictionResponse> runPredictions() {
        List<MatchesResponse> matches = getMatches();
        List<String> prompts = aiService.createPrompts(matches);

        return aiService.getAnswerFromChatModel(chatModels, prompts);
    }

    public List<MatchesResponse> getMatches() {
        List<MatchesResponse> matchesAllLeagues = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (String competition : competitions) {
            log.debug("Getting matches for {} for {}", competition, today);

            MatchesResponse matchesOneLeague = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/competitions/{competition}/matches")
                            .queryParam("dateFrom", today)
                            .queryParam("dateTo", today)
                            .build(competition))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(MatchesResponse.class);
            if (matchesOneLeague != null) {
                matchesAllLeagues.add(matchesOneLeague);
            }
        }

        return matchesAllLeagues;
    }
}