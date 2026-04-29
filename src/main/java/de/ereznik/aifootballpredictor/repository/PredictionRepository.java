package de.ereznik.aifootballpredictor.repository;

import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends CrudRepository<PredictionEntity, Long> {
    List<PredictionEntity> findByMatchId(long id);
    Optional<PredictionEntity> findFirstByOrderByCreatedAtDesc();
    boolean existsByMatchIdAndPredictionModel(Long matchId, String predictionModel);
}