package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.entity.MatchEntity;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.repository.PredictionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ResultPersistenceService {
    private final PredictionRepository predictionRepository;

    public ResultPersistenceService(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    @Transactional
    public void persist(List<MatchesResponse> matchesByCompetition) {
        for (MatchesResponse matches : matchesByCompetition) {
            for (MatchesResponse.Match match : matches.matches()) {
                List<MatchEntity> matchEntities = predictionRepository.findByGameId(match.id());
                for (MatchEntity matchEntity : matchEntities) {
                    matchEntity.setHomeGoalsScored(match.score().fullTime().home());
                    matchEntity.setAwayGoalsScored(match.score().fullTime().away());
                }
            }
        }
    }
}