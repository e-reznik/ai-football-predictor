package de.ereznik.aifootballpredictor.service;


import de.ereznik.aifootballpredictor.client.AiClient;
import de.ereznik.aifootballpredictor.dto.ai.AiRequest;
import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;
import de.ereznik.aifootballpredictor.dto.football.Competition;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AIService {

    private final AiClient aiClient;
    private final String promptPrefix;
    private final String promptSuffix;
    private final String jsonExampleStructure;

    public AIService(AiClient aiClient, @Value("${ai.prompt.prefix}") String promptPrefix,
                     @Value("${ai.prompt.suffix}") String promptSuffix) {
        this.aiClient = aiClient;
        this.promptPrefix = promptPrefix;
        this.promptSuffix = promptSuffix;
        try {
            jsonExampleStructure = new ClassPathResource("example-structure.json")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading example-structure.json", e);
            throw new IllegalStateException("Could not load example-structure.json", e);
        }
    }

    public Map<Competition, String> createPrompts(List<MatchesResponse> matchesList) {
        Map<Competition, String> promptsPerCompetition = new LinkedHashMap<>();

        for (MatchesResponse matches : matchesList) {
            Competition competition = Competition.valueOf(matches.competition().code());
            StringBuilder result = new StringBuilder();

            result.append(promptPrefix).append(" ").append(matches.competition().name()).append(":\n");
            for (MatchesResponse.Match match : matches.matches()) {
                result.append(match.homeTeam().name()).append(" vs. ").append(match.awayTeam().name()).append(" on ").append(match.utcDate()).append("\n");
            }
            result.append(promptSuffix).append("\n");
            result.append(jsonExampleStructure);

            promptsPerCompetition.put(competition, result.toString());
        }
        log.debug("Prompts created: {}", promptsPerCompetition);
        return promptsPerCompetition;
    }

    public List<PredictionResponse> getAnswerFromChatModel(List<ChatModel> chatModels, Map<Competition, String> prompts) {
        List<PredictionResponse> predictions = new ArrayList<>();

        for (ChatModel chatModel : chatModels) {
            for (Map.Entry<Competition, String> entry : prompts.entrySet()) {
                try {
                    PredictionResponse predictionResponse = aiClient.retrieveResponseFromModel(new AiRequest(chatModel, entry.getKey(), entry.getValue()));
                    if (predictionResponse != null) {
                        predictions.add(predictionResponse);
                    }
                } catch (RuntimeException e) {
                    log.error("Error parsing response from {}", chatModel, e);
                }
            }
        }
        return predictions;
    }
}