package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.client.AiClient;
import de.ereznik.aifootballpredictor.dto.ai.AiRequest;
import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;
import de.ereznik.aifootballpredictor.dto.football.Competition;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.news.NewsPerMatchPerCompetition;
import de.ereznik.aifootballpredictor.dto.news.NewsSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.ereznik.aifootballpredictor.util.JsonUtils.buildJsonStructure;

@Service
@Slf4j
public class AIService {

    private final AiClient aiClient;
    private final String promptPart1;
    private final String promptPart2;
    private final String promptPart3;
    private final List<ChatModel> chatModels;

    public AIService(AiClient aiClient,
                     @Value("${ai.prompt.part.1}") String promptPart1,
                     @Value("${ai.prompt.part.2}") String promptPart2,
                     @Value("${ai.prompt.part.3}") String promptPart3, List<ChatModel> chatModels) {
        this.aiClient = aiClient;
        this.promptPart1 = promptPart1;
        this.promptPart2 = promptPart2;
        this.promptPart3 = promptPart3;
        this.chatModels = chatModels;
        log.info("Loaded ChatModels: {}", chatModels.stream()
                .map(m -> m.getClass().getSimpleName() + "(" + m.getDefaultOptions().getModel() + ")")
                .toList());
    }

    public Map<Competition, String> createPrompts(NewsPerMatchPerCompetition newsPerMatchPerCompetition) {
        Map<Competition, String> promptsPerCompetition = new HashMap<>();

        for (var entry : newsPerMatchPerCompetition.newsPerMatchPerCompetition().entrySet()) {
            StringBuilder promptPerCompetition = new StringBuilder();

            Competition competition = entry.getKey();
            String competitionName = Competition.valueOf(competition.toString()).getDisplayName();

            promptPerCompetition.append(promptPart1).append("\n\n");

            promptPerCompetition.append("Competition: ").append(competitionName).append("\n");
            promptPerCompetition.append("Date: ").append(LocalDate.now()).append("\n");

            promptPerCompetition.append(promptPart2).append("\n\n");

            for (var entry2 : entry.getValue().entrySet()) {
                MatchesResponse.Match match = entry2.getKey();
                NewsSearchResponse newsSearchResult = entry2.getValue();

                promptPerCompetition.append("\n--- ").append(match.homeTeam().name()).append(" vs. ").append(match.awayTeam().name()).append(" ---\n");
                if (newsSearchResult.news() != null && !newsSearchResult.news().results().isEmpty()) {
                    for (var newsResult : newsSearchResult.news().results()) {
                        promptPerCompetition.append("\nTitle: ").append(newsResult.title()).append("\n");
                        if (!newsResult.extraSnippets().isEmpty()) {
                            for (var extraSnippets : newsResult.extraSnippets()) {
                                promptPerCompetition.append("- ").append(extraSnippets).append("\n");
                            }
                        }
                    }
                }
                if (newsSearchResult.web() != null && !newsSearchResult.web().results().isEmpty()) {
                    for (var webResult : newsSearchResult.web().results()) {
                        promptPerCompetition.append("\nTitle: ").append(webResult.title()).append("\n");
                        if (webResult.extraSnippets() != null && !webResult.extraSnippets().isEmpty()) {
                            for (var extraSnippets : webResult.extraSnippets()) {
                                promptPerCompetition.append("- ").append(extraSnippets).append("\n");
                            }
                        }
                    }
                }
            }
            promptPerCompetition.append("\n").append(promptPart3).append("\n");
            promptPerCompetition.append(buildJsonStructure(entry.getValue().keySet().stream().toList()));
            promptsPerCompetition.put(competition, promptPerCompetition.toString());
        }
        log.debug("Prompts created: {}", promptsPerCompetition);

        return promptsPerCompetition;
    }
    
    public List<PredictionResponse> getAnswerFromChatModel(Map<Competition, String> promptsPerCompetition) {
        List<PredictionResponse> predictions = new ArrayList<>();
        for (ChatModel chatModel : chatModels) {
            for (Map.Entry<Competition, String> entry : promptsPerCompetition.entrySet()) {
                try {
                    PredictionResponse predictionResponse = aiClient.retrieveResponseFromModel(new AiRequest(chatModel, Competition.valueOf(entry.getKey().toString()), entry.getValue()));
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