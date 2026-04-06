package de.ereznik.aifootballpredictor.dto.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "prediction", uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "predictionModel"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false, updatable = false)
    private MatchEntity match;

    @Column(nullable = false)
    private String predictionModel;

    private Integer homeGoalsPredicted;
    private Integer awayGoalsPredicted;
    private Integer probability;
    private Integer score;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}