package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;
import de.ereznik.aifootballpredictor.dto.entity.MatchEntity;
import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.repository.MatchRepository;
import de.ereznik.aifootballpredictor.repository.PredictionRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionPersistenceServiceTest {

    @Test
    void persistsMatchWhenFootballDataReturnsNullMatchday() {
        AtomicReference<MatchEntity> savedMatch = new AtomicReference<>();
        List<PredictionEntity> savedPredictions = new ArrayList<>();
        MatchRepository matchRepository = matchRepository(savedMatch);
        PredictionRepository predictionRepository = predictionRepository(savedPredictions);
        PredictionPersistenceService service = new PredictionPersistenceService(matchRepository, predictionRepository);
        MatchesResponse.Match arsenalPsgFinal = new MatchesResponse.Match(
                552096L,
                OffsetDateTime.parse("2025-05-31T19:00:00Z"),
                "FINISHED",
                null,
                new MatchesResponse.Competition(2001L, "UEFA Champions League", "CL"),
                new MatchesResponse.Match.Team(57L, "Arsenal"),
                new MatchesResponse.Match.Team(524L, "Paris Saint-Germain"),
                null
        );
        MatchesResponse matchesResponse = new MatchesResponse(List.of(arsenalPsgFinal));
        PredictionResponse predictionResponse = PredictionResponse.builder()
                .modelName("test-model")
                .competition(de.ereznik.aifootballpredictor.dto.football.Competition.CL)
                .matches(List.of(new PredictionResponse.Match(
                        "Arsenal",
                        "Paris Saint-Germain",
                        57L,
                        524L,
                        1,
                        2,
                        7
                )))
                .build();

        service.persist(List.of(matchesResponse), List.of(predictionResponse));

        assertThat(savedMatch.get().getGameId()).isEqualTo(552096L);
        assertThat(savedMatch.get().getGameDay()).isNull();
        assertThat(savedMatch.get().getTeamHome()).isEqualTo("Arsenal");
        assertThat(savedMatch.get().getTeamAway()).isEqualTo("Paris Saint-Germain");
        assertThat(savedMatch.get().getStatus()).isEqualTo("FINISHED");
        assertThat(savedPredictions).hasSize(1);
        assertThat(savedPredictions.getFirst().getMatch().getGameDay()).isNull();
        assertThat(savedPredictions.getFirst().getHomeGoalsPredicted()).isEqualTo(1);
        assertThat(savedPredictions.getFirst().getAwayGoalsPredicted()).isEqualTo(2);
    }

    private MatchRepository matchRepository(AtomicReference<MatchEntity> savedMatch) {
        return (MatchRepository) Proxy.newProxyInstance(
                MatchRepository.class.getClassLoader(),
                new Class<?>[]{MatchRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByGameId" -> Optional.empty();
                    case "save" -> {
                        MatchEntity matchEntity = (MatchEntity) args[0];
                        matchEntity.setId(1L);
                        savedMatch.set(matchEntity);
                        yield matchEntity;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private PredictionRepository predictionRepository(List<PredictionEntity> savedPredictions) {
        return (PredictionRepository) Proxy.newProxyInstance(
                PredictionRepository.class.getClassLoader(),
                new Class<?>[]{PredictionRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "existsByMatchIdAndPredictionModel" -> false;
                    case "saveAll" -> {
                        ((Iterable<PredictionEntity>) args[0]).forEach(savedPredictions::add);
                        yield savedPredictions;
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
