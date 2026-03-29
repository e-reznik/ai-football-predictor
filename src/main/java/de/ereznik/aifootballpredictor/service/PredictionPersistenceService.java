package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.ml.PredictionResponse;
import de.ereznik.aifootballpredictor.repository.PredictionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PredictionPersistenceService {
    private final PredictionRepository predictionRepository;

    public PredictionPersistenceService(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    public void persist(List<MatchesResponse> matchesByCompetition, Map<String, PredictionResponse> predictionResponses) {
        List<PredictionEntity> predictionEntities = new ArrayList<>();

        for (MatchesResponse matches : matchesByCompetition) {
            for (MatchesResponse.Match match : matches.matches()) {
                PredictionEntity predictionEntityBase = buildPredictionEntityBase(matches.competition().name(), match);
                for (Map.Entry<String, PredictionResponse> matchesPerCompetition : predictionResponses.entrySet()) {
                    predictionEntities.add(completePredictionEntity(matchesPerCompetition, predictionEntityBase));
                }
            }
        }
        predictionRepository.saveAll(predictionEntities);
    }

    /**
     * Creates the base of the PredictionEntity, with general data, without model-specific details.
     */
    private PredictionEntity buildPredictionEntityBase(String competitionName, MatchesResponse.Match match) {
        return PredictionEntity.builder()
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
    private PredictionEntity completePredictionEntity(Map.Entry<String, PredictionResponse> entry, PredictionEntity predictionEntityBase) {
        PredictionResponse.Match matchPrediction = findByTeam(entry.getValue(), predictionEntityBase.getTeamHome(), predictionEntityBase.getTeamAway());
        if (matchPrediction == null) {
            return null;
        }

        return predictionEntityBase.toBuilder()
                .predictionModel(entry.getKey())
                .homeGoalsPredicted(matchPrediction.scores().home())
                .awayGoalsPredicted(matchPrediction.scores().away())
                .probability(matchPrediction.scores().probability())
                .build();
    }

    /**
     * Finds and returns the model-specific predictions, corresponding to the match.
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
