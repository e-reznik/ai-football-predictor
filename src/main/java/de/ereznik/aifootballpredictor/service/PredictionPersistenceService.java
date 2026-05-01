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
                        .orElseGet(() -> matchRepository.save(buildMatchEntityBase(match.competition().name(), match)));

                for (PredictionResponse predictionResponse : predictionResponses) {
                    if (predictionResponse.competition() == null || !predictionResponse.competition().name().equals(match.competition().code()))
                        continue;
                    String modelName = predictionResponse.modelName();
                    if (predictionRepository.existsByMatchIdAndPredictionModel(matchEntityBase.getId(), modelName)) {
                        log.debug("Prediction already exists for match {} and model {}, skipping", matchEntityBase.getId(), modelName);
                        continue;
                    }
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
                .homeTeamId(match.homeTeam().id())
                .awayTeamId(match.awayTeam().id())
                .build();
    }

    /**
     * Completes the PredictionEntity, by inserting model-specific details.
     */
    private PredictionEntity completePredictionEntity(String modelName, PredictionResponse predictionResponse, MatchEntity matchEntityBase) {
        PredictionResponse.Match matchPrediction = findTeamById(predictionResponse, matchEntityBase.getHomeTeamId(), matchEntityBase.getAwayTeamId());
        if (matchPrediction == null) {
            log.warn("Mapping by team ID failed for '{} vs {}' by {}, falling back to name", matchEntityBase.getTeamHome(), matchEntityBase.getTeamAway(), modelName);
            matchPrediction = findTeamByName(predictionResponse, matchEntityBase.getTeamHome(), matchEntityBase.getTeamAway());
        }
        if (matchPrediction == null) {
            log.warn("Mapping by team name also failed for '{} vs {}' by {}. Response: {}", matchEntityBase.getTeamHome(), matchEntityBase.getTeamAway(), modelName, predictionResponse);
            return null;
        }

        return PredictionEntity.builder()
                .match(matchEntityBase)
                .predictionModel(modelName)
                .homeGoalsPredicted(matchPrediction.predictedHome())
                .awayGoalsPredicted(matchPrediction.predictedAway())
                .probability(matchPrediction.confidence())
                .build();
    }

    private PredictionResponse.Match findTeamById(PredictionResponse predictionResponse, Long homeTeamId, Long awayTeamId) {
        if (homeTeamId == null || awayTeamId == null) return null;
        for (PredictionResponse.Match match : predictionResponse.matches()) {
            if (homeTeamId.equals(match.homeId()) && awayTeamId.equals(match.awayId())) {
                return match;
            }
        }
        return null;
    }

    /**
     * Finds and returns the model-specific predictions, corresponding to the match. Using OR rather than AND, because some team names could be spelled differently by the models.
     */
    private PredictionResponse.Match findTeamByName(PredictionResponse predictionResponse, String homeTeam, String awayTeam) {
        for (PredictionResponse.Match match : predictionResponse.matches()) {
            if (match.home().equals(homeTeam) || match.away().equals(awayTeam)) {
                return match;
            }
        }
        return null;
    }
}