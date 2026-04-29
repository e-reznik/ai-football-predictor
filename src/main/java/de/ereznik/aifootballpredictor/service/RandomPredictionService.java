package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;
import de.ereznik.aifootballpredictor.dto.football.Competition;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
@Service
public class RandomPredictionService {

    public static final String MODEL_NAME = "Random";

    private static final Random RANDOM = new Random();
    private final List<Scoreline> scorelines;
    private final double totalWeight;

    public RandomPredictionService() {
        this.scorelines = loadFromCsv();
        double sum = 0.0;
        for (Scoreline scoreline : scorelines) {
            double weight = scoreline.weight();
            sum += weight;
        }
        this.totalWeight = sum;
    }

    public List<PredictionResponse> predict(List<MatchesResponse> matchesByCompetition) {
        List<PredictionResponse> responses = new ArrayList<>();
        for (MatchesResponse matchesResponse : matchesByCompetition) {
            Map<MatchesResponse.Competition, List<MatchesResponse.Match>> byCompetition = new LinkedHashMap<>();
            for (MatchesResponse.Match match : matchesResponse.matches()) {
                byCompetition.computeIfAbsent(match.competition(), k -> new ArrayList<>()).add(match);
            }
            for (var entry : byCompetition.entrySet()) {
                Competition competition = Competition.valueOf(entry.getKey().code());
                List<PredictionResponse.Match> matchPredictions = new ArrayList<>();
                for (MatchesResponse.Match match : entry.getValue()) {
                    int[] score = predictScore();
                    matchPredictions.add(new PredictionResponse.Match(
                            match.homeTeam().name(),
                            match.awayTeam().name(),
                            match.homeTeam().id(),
                            match.awayTeam().id(),
                            score[0],
                            score[1],
                            0
                    ));
                }
                responses.add(PredictionResponse.builder()
                        .modelName(MODEL_NAME)
                        .competition(competition)
                        .matches(matchPredictions)
                        .build());
            }
        }
        return responses;
    }

    private List<Scoreline> loadFromCsv() {
        InputStream is = getClass().getResourceAsStream("/score-frequencies.csv");
        if (is == null) {
            log.error("score-frequencies.csv not found on classpath");
            throw new IllegalStateException("score-frequencies.csv not found on classpath");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            return reader.lines()
                    .skip(1) // header
                    .filter(line -> !line.isBlank())
                    .map(line -> {
                        String[] parts = line.split(";");
                        String[] score = parts[0].split("-");
                        return new Scoreline(
                                Integer.parseInt(score[0]),
                                Integer.parseInt(score[1]),
                                Double.parseDouble(parts[1])
                        );
                    })
                    .toList();

        } catch (IOException e) {
            log.error("Failed to load scorelines.csv", e);
            throw new IllegalStateException("Failed to load scorelines.csv", e);
        }
    }

    public int[] predictScore() {
        double roll = RANDOM.nextDouble() * totalWeight;
        double cumulative = 0;
        for (Scoreline s : scorelines) {
            cumulative += s.weight;
            if (roll < cumulative) {
                return new int[]{s.home, s.away};
            }
        }
        return new int[]{1, 0}; // fallback
    }

    private record Scoreline(int home, int away, double weight) {
    }
}