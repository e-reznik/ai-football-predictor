package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;
import de.ereznik.aifootballpredictor.dto.entity.MatchEntity;
import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.repository.MatchRepository;
import de.ereznik.aifootballpredictor.repository.PredictionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PredictionPersistenceService {
    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;

    public PredictionPersistenceService(MatchRepository matchRepository, PredictionRepository predictionRepository) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
    }

    public void persist(List<MatchesResponse> matchesByCompetition, List<PredictionResponse> predictionResponses) {
        List<PredictionEntity> predictionEntities = new ArrayList<>();

        for (MatchesResponse matches : matchesByCompetition) {
            for (MatchesResponse.Match match : matches.matches()) {
                MatchEntity matchEntityBase = matchRepository.findByGameId(match.id())
                        .orElseGet(() -> matchRepository.save(buildMatchEntityBase(matches.competition().name(), match)));

                for (PredictionResponse predictionResponse : predictionResponses) {
                    String modelName = predictionResponse.chatModel().getDefaultOptions().getModel();
                    PredictionEntity predictionEntity = completePredictionEntity(modelName, predictionResponse, matchEntityBase);
                    if (predictionEntity != null) {
                        predictionEntities.add(predictionEntity);
                    }
                }
            }
        }
        predictionRepository.saveAll(predictionEntities);
    }

    /**
     * Creates the base of the MatchEntity, with general data, without model-specific details.
     */
    private MatchEntity buildMatchEntityBase(String competitionName, MatchesResponse.Match match) {
        return MatchEntity.builder()
                .gameId(match.id())
                .competitionName(competitionName)
                .gameDay(match.matchday())
                .teamHome(match.homeTeam().name())
                .teamAway(match.awayTeam().name())
                .build();
    }

    /**
     * Completes the PredictionEntity, by inserting model-specific details.
     */
    private PredictionEntity completePredictionEntity(String modelName, PredictionResponse predictionResponse, MatchEntity matchEntityBase) {
        PredictionResponse.Match matchPrediction = findByTeam(predictionResponse, matchEntityBase.getTeamHome(), matchEntityBase.getTeamAway());
        if (matchPrediction == null) {
            return null;
        }

        return PredictionEntity.builder()
                .match(matchEntityBase)
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
        for (PredictionResponse.Match match : predictionResponse.matches()) {
            if (match.teams().home().equals(homeTeam) || match.teams().away().equals(awayTeam)) {
                return match;
            }
        }
        return null;
    }
}
