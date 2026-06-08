package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.entity.MatchEntity;
import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.repository.MatchRepository;
import de.ereznik.aifootballpredictor.repository.PredictionRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ResultPersistenceServiceTest {

    @Test
    void scoresActualFinishedMatches() {
        MatchEntity matchEntity = matchEntity();
        PredictionEntity predictionEntity = PredictionEntity.builder()
                .homeGoalsPredicted(0)
                .awayGoalsPredicted(2)
                .build();
        ResultPersistenceService service = service(matchEntity, List.of(predictionEntity));

        service.persist(List.of(new MatchesResponse(List.of(match("FINISHED", 0, 2)))));

        assertThat(matchEntity.getStatus()).isEqualTo("FINISHED");
        assertThat(matchEntity.getHomeGoalsScored()).isZero();
        assertThat(matchEntity.getAwayGoalsScored()).isEqualTo(2);
        assertThat(predictionEntity.getScore()).isEqualTo(3);
    }

    @Test
    void updatesStatusButDoesNotScoreAwardedMatches() {
        MatchEntity matchEntity = matchEntity();
        matchEntity.setHomeGoalsScored(2);
        matchEntity.setAwayGoalsScored(1);

        PredictionEntity predictionEntity = PredictionEntity.builder()
                .homeGoalsPredicted(3)
                .awayGoalsPredicted(0)
                .score(1)
                .build();
        ResultPersistenceService service = service(matchEntity, List.of(predictionEntity));

        service.persist(List.of(new MatchesResponse(List.of(match("AWARDED", 3, 0)))));

        assertThat(matchEntity.getStatus()).isEqualTo("AWARDED");
        assertThat(matchEntity.getHomeGoalsScored()).isNull();
        assertThat(matchEntity.getAwayGoalsScored()).isNull();
        assertThat(predictionEntity.getScore()).isNull();
    }

    private ResultPersistenceService service(MatchEntity matchEntity, List<PredictionEntity> predictionEntities) {
        return new ResultPersistenceService(matchRepository(matchEntity), predictionRepository(predictionEntities));
    }

    private MatchEntity matchEntity() {
        return MatchEntity.builder()
                .id(1L)
                .gameId(542703L)
                .competitionName("Ligue 1")
                .teamHome("Lorient")
                .teamAway("Le Havre")
                .status("SCHEDULED")
                .build();
    }

    private MatchesResponse.Match match(String status, Integer homeGoals, Integer awayGoals) {
        return new MatchesResponse.Match(
                542703L,
                OffsetDateTime.parse("2026-05-17T19:00:00Z"),
                status,
                34,
                new MatchesResponse.Competition(2015L, "Ligue 1", "FL1"),
                new MatchesResponse.Match.Team(525L, "Lorient"),
                new MatchesResponse.Match.Team(533L, "Le Havre"),
                new MatchesResponse.Match.Score(new MatchesResponse.Match.Score.FullTime(homeGoals, awayGoals))
        );
    }

    private MatchRepository matchRepository(MatchEntity matchEntity) {
        return (MatchRepository) Proxy.newProxyInstance(
                MatchRepository.class.getClassLoader(),
                new Class<?>[]{MatchRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByGameId" -> Optional.of(matchEntity);
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private PredictionRepository predictionRepository(List<PredictionEntity> predictionEntities) {
        return (PredictionRepository) Proxy.newProxyInstance(
                PredictionRepository.class.getClassLoader(),
                new Class<?>[]{PredictionRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByMatchId" -> predictionEntities;
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
