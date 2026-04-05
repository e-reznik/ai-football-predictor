package de.ereznik.aifootballpredictor.repository;

import de.ereznik.aifootballpredictor.dto.entity.MatchEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PredictionRepository extends CrudRepository<MatchEntity, Long> {
    List<MatchEntity> findByGameId(Integer gameId);

}
