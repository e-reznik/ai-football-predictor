package de.ereznik.aifootballpredictor.repository;

import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PredictionRepository extends CrudRepository<PredictionEntity, Long> {
    List<PredictionEntity> findByMatchId(long id);
}