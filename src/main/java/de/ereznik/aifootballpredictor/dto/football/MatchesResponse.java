package de.ereznik.aifootballpredictor.dto.football;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record MatchesResponse(List<Match> matches) {

    public record Competition(Long id, String name, String code) {
    }

    public record Match(
            Long id,
            OffsetDateTime utcDate,
            Integer matchday,
            Competition competition,
            Team homeTeam,
            Team awayTeam,
            Score score
    ) {
        public record Team(Long id, @JsonProperty("shortName") String name) {
        }

        public record Score(FullTime fullTime) {
            public record FullTime(Integer home, Integer away) {
            }
        }
    }
}