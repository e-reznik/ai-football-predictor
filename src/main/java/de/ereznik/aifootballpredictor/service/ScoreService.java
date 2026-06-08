package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.dashboard.ConsensusPredictionView;
import de.ereznik.aifootballpredictor.dto.dashboard.DashboardData;
import de.ereznik.aifootballpredictor.dto.dashboard.PastMatchView;
import de.ereznik.aifootballpredictor.dto.dashboard.UpcomingMatchView;
import de.ereznik.aifootballpredictor.dto.entity.MatchEntity;
import de.ereznik.aifootballpredictor.dto.entity.PredictionEntity;
import de.ereznik.aifootballpredictor.repository.MatchRepository;
import de.ereznik.aifootballpredictor.repository.PredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ScoreService {
    private static final Set<String> NOT_UPCOMING_STATUSES = Set.of(
            "FINISHED",
            "AWARDED",
            "SUSPENDED",
            "CANCELLED",
            "POSTPONED"
    );

    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;

    public ScoreService(PredictionRepository predictionRepository, MatchRepository matchRepository) {
        this.predictionRepository = predictionRepository;
        this.matchRepository = matchRepository;
    }

    public DashboardData getDashboardData() {
        Iterable<PredictionEntity> all = predictionRepository.findAll();

        Map<String, Map<String, Integer>> totals = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> scoredCountByCompetition = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, Integer>>> accuracyByCompetition = new LinkedHashMap<>();
        Map<String, TreeMap<Integer, Map<String, Integer>>> byMatchday = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> accuracy = new LinkedHashMap<>();
        Map<String, Integer> predictionCount = new LinkedHashMap<>();
        Map<String, Integer> scoredCount = new LinkedHashMap<>();
        Map<String, Integer> totalPoints = new LinkedHashMap<>();
        Set<String> allCompetitions = new LinkedHashSet<>();
        Set<String> allModels = new LinkedHashSet<>();

        for (PredictionEntity p : all) {
            String model = p.getPredictionModel();
            predictionCount.merge(model, 1, (a, b) -> a + b);
            allModels.add(model);
            allCompetitions.add(p.getMatch().getCompetitionName());

            if (p.getScore() == null) continue;

            String competition = p.getMatch().getCompetitionName();
            Integer matchday = p.getMatch().getGameDay();
            int score = p.getScore();

            scoredCount.merge(model, 1, (a, b) -> a + b);
            totalPoints.merge(model, score, (a, b) -> a + b);
            addTotal(totals, competition, model, score);
            addScoredCount(scoredCountByCompetition, competition, model);
            if (matchday != null) {
                addByMatchday(byMatchday, competition, matchday, model, score);
            }
            addAccuracy(accuracy, model, score);
            addAccuracy(accuracyByCompetition, competition, model, score);
        }

        Map<String, Double> avgPointsPerGame = new LinkedHashMap<>();
        for (String m : allModels) {
            int scored = scoredCount.getOrDefault(m, 0);
            avgPointsPerGame.put(m, scored > 0 ? (double) totalPoints.getOrDefault(m, 0) / scored : 0.0);
        }

        List<String> competitions = new ArrayList<>(allCompetitions);
        List<String> models = new ArrayList<>(allModels);
        Map<String, Map<Integer, Map<String, Integer>>> cumulative = buildCumulative(competitions, models, byMatchday);

        String trackingSince = matchRepository.findFirstByOrderByCreatedAtAsc()
                .map(m -> m.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .orElse(null);

        long totalGames = matchRepository.count();

        DateTimeFormatter ts = DateTimeFormatter.ofPattern("d MMM HH:mm");
        String lastPredictionRun = predictionRepository.findFirstByOrderByCreatedAtDesc()
                .map(p -> p.getCreatedAt().format(ts))
                .orElse(null);
        String lastResultsFetched = matchRepository.findFirstByHomeGoalsScoredIsNotNullOrderByUpdatedAtDesc()
                .map(m -> m.getUpdatedAt().format(ts))
                .orElse(null);

        return new DashboardData(competitions, models, totals, scoredCountByCompetition, accuracyByCompetition,
                cumulative, accuracy, predictionCount, scoredCount,
                avgPointsPerGame, trackingSince, totalGames, lastPredictionRun, lastResultsFetched);
    }

    private void addTotal(Map<String, Map<String, Integer>> totals, String competition, String model, int score) {
        totals.computeIfAbsent(competition, k -> new LinkedHashMap<>())
                .merge(model, score, (a, b) -> a + b);
    }

    private void addScoredCount(Map<String, Map<String, Integer>> scoredCountByCompetition, String competition, String model) {
        scoredCountByCompetition.computeIfAbsent(competition, k -> new LinkedHashMap<>())
                .merge(model, 1, (a, b) -> a + b);
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

    private void addAccuracy(Map<String, Map<String, Map<String, Integer>>> accuracyByCompetition,
                             String competition, String model, int score) {
        String bucket = switch (score) {
            case 3 -> "exact";
            case 1 -> "tendency";
            default -> "wrong";
        };
        accuracyByCompetition.computeIfAbsent(competition, k -> new LinkedHashMap<>())
                .computeIfAbsent(model, k -> new LinkedHashMap<>(Map.of("exact", 0, "tendency", 0, "wrong", 0)))
                .merge(bucket, 1, (a, b) -> a + b);
    }

    private Map<String, Map<Integer, Map<String, Integer>>> buildCumulative(
            List<String> competitions, List<String> models,
            Map<String, TreeMap<Integer, Map<String, Integer>>> byMatchday) {
        Map<String, Map<Integer, Map<String, Integer>>> cumulative = new LinkedHashMap<>();
        for (String comp : competitions) {
            if (!byMatchday.containsKey(comp)) continue;
            Map<Integer, Map<String, Integer>> cumulativeMatchdays = new LinkedHashMap<>();
            Map<String, Integer> running = new HashMap<>();
            for (Map.Entry<Integer, Map<String, Integer>> entry : byMatchday.get(comp).entrySet()) {
                Map<String, Integer> cumulativeForMatchday = new LinkedHashMap<>();
                for (String model : models) {
                    Integer score = entry.getValue().get(model);
                    if (score != null) {
                        running.merge(model, score, (a, b) -> a + b);
                        cumulativeForMatchday.put(model, running.get(model));
                    }
                }
                cumulativeMatchdays.put(entry.getKey(), cumulativeForMatchday);
            }
            cumulative.put(comp, cumulativeMatchdays);
        }
        return cumulative;
    }

    @Transactional(readOnly = true)
    public List<PastMatchView> getPastPredictions() {
        List<MatchEntity> finishedMatches = matchRepository.findByHomeGoalsScoredIsNotNull();
        List<PastMatchView> result = new ArrayList<>();
        for (MatchEntity match : finishedMatches) {
            if (match.getPredictions() == null || match.getPredictions().isEmpty()) continue;
            Map<String, String> predictions = new LinkedHashMap<>();
            Map<String, Integer> scores = new LinkedHashMap<>();
            for (PredictionEntity p : match.getPredictions()) {
                if (p.getHomeGoalsPredicted() != null && p.getAwayGoalsPredicted() != null) {
                    predictions.put(p.getPredictionModel(), p.getHomeGoalsPredicted() + " – " + p.getAwayGoalsPredicted());
                }
                if (p.getScore() != null) {
                    scores.put(p.getPredictionModel(), p.getScore());
                }
            }
            if (!predictions.isEmpty()) {
                result.add(new PastMatchView(
                        match.getCompetitionName(),
                        match.getGameDay(),
                        match.getTeamHome(),
                        match.getTeamAway(),
                        match.getHomeGoalsScored(),
                        match.getAwayGoalsScored(),
                        predictions,
                        scores
                ));
            }
        }
        result.sort((a, b) -> !a.competition().equals(b.competition())
                ? a.competition().compareTo(b.competition())
                : Comparator.nullsLast(Comparator.<Integer>reverseOrder()).compare(a.gameDay(), b.gameDay()));
        return result;
    }

    @Transactional(readOnly = true)
    public List<UpcomingMatchView> getUpcomingMatches() {
        List<MatchEntity> upcomingEntities = matchRepository.findByHomeGoalsScoredIsNull();
        List<UpcomingMatchView> result = new ArrayList<>();
        for (MatchEntity match : upcomingEntities) {
            String status = match.getStatus();
            if (status != null && NOT_UPCOMING_STATUSES.contains(status)) continue;
            if (match.getPredictions() == null || match.getPredictions().isEmpty()) continue;
            Map<String, String> predictions = new LinkedHashMap<>();
            Map<String, Integer> probabilities = new LinkedHashMap<>();
            for (PredictionEntity p : match.getPredictions()) {
                if (p.getHomeGoalsPredicted() != null && p.getAwayGoalsPredicted() != null) {
                    predictions.put(p.getPredictionModel(), p.getHomeGoalsPredicted() + " – " + p.getAwayGoalsPredicted());
                    if (p.getProbability() != null) {
                        probabilities.put(p.getPredictionModel(), p.getProbability());
                    }
                }
            }
            if (!predictions.isEmpty()) {
                result.add(new UpcomingMatchView(
                        match.getCompetitionName(),
                        match.getGameDay(),
                        match.getTeamHome(),
                        match.getTeamAway(),
                        buildConsensusPrediction(match.getPredictions()),
                        predictions,
                        probabilities
                ));
            }
        }
        result.sort(Comparator.comparing(UpcomingMatchView::competition)
                .thenComparing(UpcomingMatchView::gameDay, Comparator.nullsLast(Integer::compareTo)));
        return result;
    }

    private ConsensusPredictionView buildConsensusPrediction(List<PredictionEntity> predictions) {
        List<PredictionEntity> weightedPredictions = predictions.stream()
                .filter(p -> !RandomPredictionService.MODEL_NAME.equals(p.getPredictionModel()))
                .filter(p -> p.getHomeGoalsPredicted() != null && p.getAwayGoalsPredicted() != null)
                .filter(p -> p.getProbability() != null && p.getProbability() > 0)
                .toList();

        if (weightedPredictions.isEmpty()) {
            return null;
        }

        int totalWeight = weightedPredictions.stream().mapToInt(PredictionEntity::getProbability).sum();
        double weightedHomeGoals = weightedPredictions.stream()
                .mapToDouble(p -> p.getHomeGoalsPredicted() * p.getProbability())
                .sum() / totalWeight;
        double weightedAwayGoals = weightedPredictions.stream()
                .mapToDouble(p -> p.getAwayGoalsPredicted() * p.getProbability())
                .sum() / totalWeight;

        String outcome = outcomeLabel(weightedHomeGoals, weightedAwayGoals);
        int agreeingModels = (int) weightedPredictions.stream()
                .filter(p -> outcome.equals(outcomeLabel(p.getHomeGoalsPredicted(), p.getAwayGoalsPredicted())))
                .count();
        int averageConfidence = (int) Math.round(weightedPredictions.stream()
                .mapToInt(PredictionEntity::getProbability)
                .average()
                .orElse(0) * 10);

        return new ConsensusPredictionView(
                String.format(Locale.US, "%.1f - %.1f", weightedHomeGoals, weightedAwayGoals),
                outcome,
                averageConfidence,
                agreeingModels,
                weightedPredictions.size()
        );
    }

    private String outcomeLabel(double homeGoals, double awayGoals) {
        if (homeGoals > awayGoals) {
            return "Home win";
        }
        if (awayGoals > homeGoals) {
            return "Away win";
        }
        return "Draw";
    }
}
