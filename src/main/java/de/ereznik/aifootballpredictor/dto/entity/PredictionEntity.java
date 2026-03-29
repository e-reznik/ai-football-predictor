package de.ereznik.aifootballpredictor.dto.entity;


import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PredictionEntity {
    // TODO: Add DB constraints
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Nullable
    private Long id;
    private String competitionName;
    private Integer gameDay;
    private Integer gameId;
    private String teamHome;
    private String teamAway;
    private Integer homeGoalsPredicted;
    private Integer awayGoalsPredicted;
    private Integer homeGoalsScored;
    private Integer awayGoalsScored;
    private Integer probability;
    private String predictionModel;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}