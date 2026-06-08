package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.entity.MatchEntity;
import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import de.ereznik.aifootballpredictor.repository.MatchRepository;
import de.ereznik.aifootballpredictor.repository.PredictionRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreServiceTest {

    @Test
    void upcomingMatchesExcludeKnownInactiveStatuses() {
        ScoreService service = new ScoreService(predictionRepository(), matchRepository(List.of(
                match("Scheduled Home", "SCHEDULED"),
                match("Legacy Home", null),
                match("Awarded Home", "AWARDED"),
                match("Suspended Home", "SUSPENDED"),
                match("Cancelled Home", "CANCELLED"),
                match("Postponed Home", "POSTPONED"),
                match("Finished Home", "FINISHED")
        )));

        assertThat(service.getUpcomingMatches())
                .extracting(match -> match.homeTeam())
                .containsExactly("Scheduled Home", "Legacy Home");
    }

    private MatchEntity match(String homeTeam, String status) {
        MatchEntity matchEntity = MatchEntity.builder()
                .competitionName("Ligue 1")
                .gameDay(34)
                .teamHome(homeTeam)
                .teamAway("Away")
                .status(status)
                .build();
        matchEntity.setPredictions(List.of(PredictionEntity.builder()
                .predictionModel("test-model")
                .homeGoalsPredicted(1)
                .awayGoalsPredicted(0)
                .probability(7)
                .build()));
        return matchEntity;
    }

    private MatchRepository matchRepository(List<MatchEntity> matches) {
        return (MatchRepository) Proxy.newProxyInstance(
                MatchRepository.class.getClassLoader(),
                new Class<?>[]{MatchRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByHomeGoalsScoredIsNull" -> matches;
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private PredictionRepository predictionRepository() {
        return (PredictionRepository) Proxy.newProxyInstance(
                PredictionRepository.class.getClassLoader(),
                new Class<?>[]{PredictionRepository.class},
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
