package de.ereznik.aifootballpredictor.dto.ml;

import java.util.List;

public record PredictionResponse(List<Match> matches) {

    public record Match(Teams teams, Scores scores) {
    }

    public record Teams(String home, String away) {
    }

    public record Scores(int home, int away, int probability) {
    }
}
