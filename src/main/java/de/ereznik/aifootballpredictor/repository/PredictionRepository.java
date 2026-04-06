package de.ereznik.aifootballpredictor.repository;

import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import org.springframework.data.repository.CrudRepository;

public interface PredictionRepository extends CrudRepository<PredictionEntity, Long> {
}