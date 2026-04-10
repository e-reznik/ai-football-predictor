package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.DashboardData;
import de.ereznik.aifootballpredictor.dto.UpcomingMatchView;
import de.ereznik.aifootballpredictor.dto.entity.MatchEntity;
import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import de.ereznik.aifootballpredictor.repository.MatchRepository;
import de.ereznik.aifootballpredictor.repository.PredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ScoreService {
    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;

    public ScoreService(PredictionRepository predictionRepository, MatchRepository matchRepository) {
        this.predictionRepository = predictionRepository;
        this.matchRepository = matchRepository;
    }

    public DashboardData getDashboardData() {
        Iterable<PredictionEntity> all = predictionRepository.findAll();

        Map<String, Map<String, Integer>> totals = new LinkedHashMap<>();
        Map<String, TreeMap<Integer, Map<String, Integer>>> byMatchday = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> accuracy = new LinkedHashMap<>();
        Map<String, Integer> predictionCount = new LinkedHashMap<>();

        for (PredictionEntity p : all) {
            String model = p.getPredictionModel();
            predictionCount.merge(model, 1, (a, b) -> a + b);

            if (p.getScore() == null) continue;

            String competition = p.getMatch().getCompetitionName();
            int matchday = p.getMatch().getGameDay();
            int score = p.getScore();

            addTotal(totals, competition, model, score);
            addByMatchday(byMatchday, competition, matchday, model, score);
            addAccuracy(accuracy, model, score);
        }

        List<String> competitions = new ArrayList<>(totals.keySet());
        List<String> models = extractModels(totals);
        Map<String, Map<Integer, Map<String, Integer>>> cumulative = buildCumulative(competitions, models, byMatchday);

        return new DashboardData(competitions, models, totals, cumulative, accuracy, predictionCount);
    }

    private void addTotal(Map<String, Map<String, Integer>> totals, String competition, String model, int score) {
        totals.computeIfAbsent(competition, k -> new LinkedHashMap<>())
                .merge(model, score, (a, b) -> a + b);
    }

    private void addByMatchday(Map<String, TreeMap<Integer, Map<String, Integer>>> byMatchday,
                               String competition, int matchday, String model, int score) {
        byMatchday.computeIfAbsent(competition, k -> new TreeMap<>())
                .computeIfAbsent(matchday, k -> new LinkedHashMap<>())
                .merge(model, score, (a, b) -> a + b);
    }

    private void addAccuracy(Map<String, Map<String, Integer>> accuracy, String model, int score) {
        String bucket = switch (score) {
            case 3 -> "exact";
            case 1 -> "tendency";
            default -> "wrong";
        };
        accuracy.computeIfAbsent(model, k -> new LinkedHashMap<>(Map.of("exact", 0, "tendency", 0, "wrong", 0)))
                .merge(bucket, 1, (a, b) -> a + b);
    }

    private List<String> extractModels(Map<String, Map<String, Integer>> totals) {
        LinkedHashSet<String> modelSet = new LinkedHashSet<>();
        totals.values().forEach(m -> modelSet.addAll(m.keySet()));
        return new ArrayList<>(modelSet);
    }

    private Map<String, Map<Integer, Map<String, Integer>>> buildCumulative(
            List<String> competitions, List<String> models,
            Map<String, TreeMap<Integer, Map<String, Integer>>> byMatchday) {
        Map<String, Map<Integer, Map<String, Integer>>> cumulative = new LinkedHashMap<>();
        for (String comp : competitions) {
            Map<Integer, Map<String, Integer>> cumulativeMatchdays = new LinkedHashMap<>();
            Map<String, Integer> running = new HashMap<>();
            for (Map.Entry<Integer, Map<String, Integer>> entry : byMatchday.get(comp).entrySet()) {
                for (String model : models) {
                    running.merge(model, entry.getValue().getOrDefault(model, 0), (a, b) -> a + b);
                }
                cumulativeMatchdays.put(entry.getKey(), new LinkedHashMap<>(running));
            }
            cumulative.put(comp, cumulativeMatchdays);
        }
        return cumulative;
    }

    @Transactional(readOnly = true)
    public List<UpcomingMatchView> getUpcomingMatches() {
        List<MatchEntity> upcomingEntities = matchRepository.findByHomeGoalsScoredIsNull();
        List<UpcomingMatchView> result = new ArrayList<>();
        for (MatchEntity match : upcomingEntities) {
            if (match.getPredictions() == null || match.getPredictions().isEmpty()) continue;
            Map<String, String> predictions = new LinkedHashMap<>();
            for (PredictionEntity p : match.getPredictions()) {
                if (p.getHomeGoalsPredicted() != null && p.getAwayGoalsPredicted() != null) {
                    predictions.put(p.getPredictionModel(), p.getHomeGoalsPredicted() + " – " + p.getAwayGoalsPredicted());
                }
            }
            if (!predictions.isEmpty()) {
                result.add(new UpcomingMatchView(
                        match.getCompetitionName(),
                        match.getGameDay(),
                        match.getTeamHome(),
                        match.getTeamAway(),
                        predictions
                ));
            }
        }
        result.sort((a, b) -> a.gameDay() != b.gameDay() ? Integer.compare(a.gameDay(), b.gameDay()) : a.homeTeam().compareTo(b.homeTeam()));
        return result;
    }
}
