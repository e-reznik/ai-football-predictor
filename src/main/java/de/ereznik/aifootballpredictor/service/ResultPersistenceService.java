package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.repository.MatchRepository;
import de.ereznik.aifootballpredictor.repository.PredictionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ResultPersistenceService {
    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;

    public ResultPersistenceService(MatchRepository matchRepository, PredictionRepository predictionRepository) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
    }

    @Transactional
    public void persist(List<MatchesResponse> matchesByCompetition) {
        for (MatchesResponse matches : matchesByCompetition) {
            for (MatchesResponse.Match match : matches.matches()) {
                matchRepository.findByGameId(match.id()).ifPresent(matchEntity -> {
                    matchEntity.setHomeGoalsScored(match.score().fullTime().home());
                    matchEntity.setAwayGoalsScored(match.score().fullTime().away());

                    List<PredictionEntity> predictionEntity = predictionRepository.findByMatchId(matchEntity.getId());
                    for (PredictionEntity predictionEntityTemp : predictionEntity) {
                        int score = calculateScore(predictionEntityTemp, match);
                        predictionEntityTemp.setScore(score);
                    }
                });
            }
        }
    }

    private int calculateScore(PredictionEntity predictionEntity, MatchesResponse.Match match) {
        if (predictionEntity.getHomeGoalsPredicted() == null || predictionEntity.getAwayGoalsPredicted() == null
                || match.score().fullTime().home() == null || match.score().fullTime().away() == null) {
            return 0;
        }
        int predictedHomeGoals = predictionEntity.getHomeGoalsPredicted();
        int predictedAwayGoals = predictionEntity.getAwayGoalsPredicted();
        int scoredHomeGoals = match.score().fullTime().home();
        int scoredAwayGoals = match.score().fullTime().away();

        if (predictedHomeGoals == scoredHomeGoals && predictedAwayGoals == scoredAwayGoals) {
            return 3; // exact score
        } else if (predictedHomeGoals > predictedAwayGoals && scoredHomeGoals > scoredAwayGoals
                || predictedHomeGoals < predictedAwayGoals && scoredHomeGoals < scoredAwayGoals
                || predictedHomeGoals == predictedAwayGoals && scoredHomeGoals == scoredAwayGoals) {
            return 1; // correct tendency
        }
        return 0;
    }
}
