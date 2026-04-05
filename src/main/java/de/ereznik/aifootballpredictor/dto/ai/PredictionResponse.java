package de.ereznik.aifootballpredictor.dto.ai;

import de.ereznik.aifootballpredictor.dto.football.Competition;
import lombok.Builder;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;

@Builder(toBuilder = true)
public record PredictionResponse(ChatModel chatModel, Competition competition, List<Match> matches) {

    public record Match(Teams teams, Scores scores) {
    }

    public record Teams(String home, String away) {
    }

    public record Scores(int home, int away, int probability) {
    }
}
