package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.repository.MatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ResultPersistenceService {
    private final MatchRepository matchRepository;

    public ResultPersistenceService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional
    public void persist(List<MatchesResponse> matchesByCompetition) {
        for (MatchesResponse matches : matchesByCompetition) {
            for (MatchesResponse.Match match : matches.matches()) {
                matchRepository.findByGameId(match.id()).ifPresent(matchEntity -> {
                    matchEntity.setHomeGoalsScored(match.score().fullTime().home());
                    matchEntity.setAwayGoalsScored(match.score().fullTime().away());
                });
            }
        }
    }
}
