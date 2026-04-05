package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;
import de.ereznik.aifootballpredictor.dto.entity.MatchEntity;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.repository.PredictionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PredictionPersistenceService {
    private final PredictionRepository predictionRepository;

    public PredictionPersistenceService(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    public void persist(List<MatchesResponse> matchesByCompetition, List<PredictionResponse> predictionResponses) {
        List<MatchEntity> predictionEntities = new ArrayList<>();

        for (MatchesResponse matches : matchesByCompetition) {
            for (MatchesResponse.Match match : matches.matches()) {
                MatchEntity matchEntityBase = buildPredictionEntityBase(matches.competition().name(), match);

                for (PredictionResponse predictionResponse : predictionResponses) {
                    String modelName = predictionResponse.chatModel().getClass().getSimpleName();
                    MatchEntity matchEntity = completePredictionEntity(modelName, predictionResponse, matchEntityBase);
                    if (matchEntity != null) {
                        predictionEntities.add(matchEntity);
                    }
                }
            }
        }
        predictionRepository.saveAll(predictionEntities);
    }

    /**
     * Creates the base of the PredictionEntity, with general data, without model-specific details.
     */
    private MatchEntity buildPredictionEntityBase(String competitionName, MatchesResponse.Match match) {
        return MatchEntity.builder()
                .competitionName(competitionName)
                .gameDay(match.matchday())
                .gameId(match.id())
                .teamHome(match.homeTeam().name())
                .teamAway(match.awayTeam().name())
                .build();
    }

    /**
     * Completes the base of the PredictionEntity, by inserting model-specific details.
     */
    private MatchEntity completePredictionEntity(String modelName, PredictionResponse predictionResponse, MatchEntity matchEntityBase) {
        PredictionResponse.Match matchPrediction = findByTeam(predictionResponse, matchEntityBase.getTeamHome(), matchEntityBase.getTeamAway());
        if (matchPrediction == null) {
            return null;
        }

        return matchEntityBase.toBuilder()
                .predictionModel(modelName)
                .homeGoalsPredicted(matchPrediction.scores().home())
                .awayGoalsPredicted(matchPrediction.scores().away())
                .probability(matchPrediction.scores().probability())
                .build();
    }

    /**
     * Finds and returns the model-specific predictions, corresponding to the match. Using OR rather than AND, because some team names could be spelled differently by the models.
     */
    private PredictionResponse.Match findByTeam(PredictionResponse predictionResponse, String homeTeam, String awayTeam) {
        for (int i = 0; i < predictionResponse.matches().size(); i++) {
            if (predictionResponse.matches().get(i).teams().home().equals(homeTeam) || predictionResponse.matches().get(i).teams().away().equals(awayTeam)) {
                return predictionResponse.matches().get(i);
            }
        }
        return null;
    }
}
