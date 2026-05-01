package de.ereznik.aifootballpredictor.dto.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "match")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long gameId;
    @Column(nullable = false)
    private String competitionName;
    @Column(nullable = false)
    private Integer gameDay;
    @Column(nullable = false)
    private String teamHome;
    @Column(nullable = false)
    private String teamAway;
    private Long homeTeamId;
    private Long awayTeamId;
    private OffsetDateTime gameDate;
    private Integer homeGoalsScored;
    private Integer awayGoalsScored;
    @OneToMany(mappedBy = "match")
    private List<PredictionEntity> predictions;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}