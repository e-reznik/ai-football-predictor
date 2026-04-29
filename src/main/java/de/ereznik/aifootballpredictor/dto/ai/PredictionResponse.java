package de.ereznik.aifootballpredictor.dto.ai;

import de.ereznik.aifootballpredictor.dto.football.Competition;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record PredictionResponse(String modelName, Competition competition, List<Match> matches) {

    public record Match(String home, String away, Long homeId, Long awayId, int predictedHome, int predictedAway,
                        int confidence) {
    }
}
