package de.ereznik.aifootballpredictor.dto.football;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record MatchesResponse(Competition competition, List<Match> matches) {

    public record Competition(int id, String name, String code) {
    }

    public record Match(int id, LocalDate utcDate, int matchday, Team homeTeam, Team awayTeam,
                        Score score) {
        public record Team(int id, @JsonProperty("shortName") String name) {
        }

        public record Score(FullTime fullTime) {
            public record FullTime(Integer home, Integer away) {
            }
        }
    }
}
