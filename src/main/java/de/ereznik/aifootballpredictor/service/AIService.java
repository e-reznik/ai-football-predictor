package de.ereznik.aifootballpredictor.service;


import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.ml.PredictionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AIService {

    private final String promptPrefix;
    private final String promptSuffix;
    private final String jsonExampleStructure;
    private final ObjectMapper objectMapper;

    public AIService(@Value("${ai.prompt.prefix}") String promptPrefix,
                     @Value("${ai.prompt.suffix}") String promptSuffix, ObjectMapper objectMapper) {
        this.promptPrefix = promptPrefix;
        this.promptSuffix = promptSuffix;
        this.objectMapper = objectMapper;

        try {
            jsonExampleStructure = new ClassPathResource("example-structure.json")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading example-structure.json", e);
            throw new IllegalStateException("Could not load example-structure.json", e);
        }
    }

    public List<String> createPrompts(List<MatchesResponse> matchesList) {
        List<String> promptsAllMatches = new ArrayList<>();

        for (MatchesResponse matches : matchesList) {
            StringBuilder result = new StringBuilder();
            String competitionName = matches.competition().name();

            result.append(promptPrefix).append(" ").append(competitionName).append(":\n");
            for (MatchesResponse.Match match : matches.matches()) {
                result.append(match.homeTeam().name()).append(" vs. ").append(match.awayTeam().name()).append(" on ").append(match.utcDate()).append("\n");
            }
            result.append(promptSuffix).append("\n");
            result.append(jsonExampleStructure);

            promptsAllMatches.add(result.toString());
        }

        log.info("Prompts created: {}", promptsAllMatches);
        return promptsAllMatches;
    }

    public Map<String, PredictionResponse> getAnswerFromChatModel(List<ChatModel> chatModels, List<String> prompts) {
        Map<String, PredictionResponse> predictions = new HashMap<>();

        for (ChatModel chatModel : chatModels) {
            for (String prompt : prompts) {
                log.trace("Getting predictions from {} for {}", chatModel, prompt);
                try {
                    PredictionResponse predictionResponse = parseJson(chatModel.call(prompt));
                    if (predictionResponse != null) {
                        predictions.put(chatModel.toString(), predictionResponse);
                    }
                } catch (RuntimeException e) {
                    log.error("Error parsing {} for {}", chatModel, prompt, e.getCause());
                }
            }
        }
        return predictions;
    }

    private PredictionResponse parseJson(String json) {
        String cleaned = json.strip();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("```\\s*$", "").strip();
        }
        try {
            return objectMapper.readValue(cleaned, PredictionResponse.class);
        } catch (RuntimeException e) {
            log.error("Error parsing response: {}", json, e);
            return null;
        }
    }
}
