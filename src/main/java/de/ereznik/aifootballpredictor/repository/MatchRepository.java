package de.ereznik.aifootballpredictor.repository;

import de.ereznik.aifootballpredictor.dto.entity.MatchEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends CrudRepository<MatchEntity, Long> {
    Optional<MatchEntity> findByGameId(Long gameId);
    List<MatchEntity> findByHomeGoalsScoredIsNull();
    List<MatchEntity> findByHomeGoalsScoredIsNotNull();
}