package de.ereznik.aifootballpredictor.dto.entity;


import jakarta.persistence.*;
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
public class MatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private Long id;
    private String competitionName;
    private Integer gameDay;
    private Integer gameId;
    @Column(nullable = false)
    private String teamHome;
    @Column(nullable = false)
    private String teamAway;
    private Integer homeGoalsPredicted;
    private Integer awayGoalsPredicted;
    private Integer homeGoalsScored;
    private Integer awayGoalsScored;
    private Integer probability;
    private String predictionModel;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}