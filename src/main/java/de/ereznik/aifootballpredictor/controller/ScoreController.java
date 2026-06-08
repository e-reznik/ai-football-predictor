package de.ereznik.aifootballpredictor.controller;

import de.ereznik.aifootballpredictor.dto.dashboard.DashboardData;
import de.ereznik.aifootballpredictor.service.AIService;
import de.ereznik.aifootballpredictor.service.RandomPredictionService;
import de.ereznik.aifootballpredictor.service.ScoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ScoreController {
    private static final int OVERALL_QUALIFIED_PREDICTIONS = 100;
    private static final int COMPETITION_QUALIFIED_PREDICTIONS = 20;

    private final ScoreService scoreService;
    private final AIService aiService;

    public ScoreController(ScoreService scoreService, AIService aiService) {
        this.scoreService = scoreService;
        this.aiService = aiService;
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/history")
    public String history(Model model) {
        var data = scoreService.getDashboardData();
        model.addAttribute("models", data.models());
        model.addAttribute("pastMatches", scoreService.getPastPredictions());
        return "history";
    }

    @GetMapping("/")
    public String scores(Model model) {
        DashboardData data = scoreService.getDashboardData();

        model.addAttribute("competitions", data.competitions());
        model.addAttribute("models", data.models());
        model.addAttribute("predictionCounts", data.predictionCountByModel());
        model.addAttribute("scoredPredictionCounts", data.scoredPredictionCountByModel());
        model.addAttribute("avgPointsPerGame", data.avgPointsPerGameByModel());
        List<String> activeModels = new ArrayList<>(aiService.getActiveModelNames());
        activeModels.add(RandomPredictionService.MODEL_NAME);
        model.addAttribute("barChartData", buildBarChartData(data));
        model.addAttribute("lineChartsData", buildLineChartsData(data));
        model.addAttribute("accuracyChartData", buildAccuracyChartData(data));
        model.addAttribute("leaderboardStats", buildLeaderboardStats(data, activeModels));
        model.addAttribute("leaderboardCompetitions", new ArrayList<>(data.totalByCompetitionAndModel().keySet()));
        model.addAttribute("upcomingMatches", scoreService.getUpcomingMatches());
        model.addAttribute("trackingSince", data.trackingSince());
        int totalPredictions = data.predictionCountByModel().values().stream().mapToInt(i -> i).sum();
        int totalScoredPredictions = data.scoredPredictionCountByModel().values().stream().mapToInt(i -> i).sum();
        model.addAttribute("totalPredictions", totalPredictions);
        model.addAttribute("hasScoredPredictions", totalScoredPredictions > 0);
        model.addAttribute("totalGames", data.totalGames());
        model.addAttribute("lastPredictionRun", data.lastPredictionRun());
        model.addAttribute("lastResultsFetched", data.lastResultsFetched());
        model.addAttribute("activeModels", activeModels);
        return "index";
    }

    private Map<String, Object> buildBarChartData(DashboardData data) {
        String[] colors = {
                "rgba(74,144,226,0.8)", "rgba(80,200,120,0.8)",
                "rgba(255,159,64,0.8)", "rgba(153,102,255,0.8)"
        };
        List<Map<String, Object>> datasets = new ArrayList<>();
        int i = 0;
        for (String comp : data.competitions()) {
            Map<String, Integer> compScores = data.totalByCompetitionAndModel().getOrDefault(comp, Map.of());
            List<Integer> values = data.models().stream().map(m -> compScores.getOrDefault(m, 0)).toList();
            datasets.add(Map.of("label", comp, "data", values, "backgroundColor", colors[i++ % colors.length]));
        }
        return Map.of("labels", data.models(), "datasets", datasets);
    }

    private Map<String, Object> buildAccuracyChartData(DashboardData data) {
        List<Integer> exact = data.models().stream().map(m -> data.accuracyByModel().getOrDefault(m, Map.of()).getOrDefault("exact", 0)).toList();
        List<Integer> tendency = data.models().stream().map(m -> data.accuracyByModel().getOrDefault(m, Map.of()).getOrDefault("tendency", 0)).toList();
        List<Integer> wrong = data.models().stream().map(m -> data.accuracyByModel().getOrDefault(m, Map.of()).getOrDefault("wrong", 0)).toList();
        return Map.of("labels", data.models(), "datasets", List.of(
                Map.of("label", "Exact score (3pts)", "data", exact, "backgroundColor", "rgba(80,200,120,0.8)"),
                Map.of("label", "Correct tendency (1pt)", "data", tendency, "backgroundColor", "rgba(255,200,64,0.8)"),
                Map.of("label", "Wrong (0pts)", "data", wrong, "backgroundColor", "rgba(220,80,80,0.8)")
        ));
    }

    private Map<String, Object> buildLeaderboardStats(DashboardData data, List<String> activeModels) {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<String> leaderboardModels = new ArrayList<>(data.models());
        for (String activeModel : activeModels) {
            if (!leaderboardModels.contains(activeModel)) {
                leaderboardModels.add(activeModel);
            }
        }
        stats.put("ALL", buildLeaderboardRows(
                leaderboardModels,
                data.scoredPredictionCountByModel(),
                data.avgPointsPerGameByModel(),
                data.totalByCompetitionAndModel().values().stream().reduce(new LinkedHashMap<>(), (totals, competitionTotals) -> {
                    competitionTotals.forEach((model, points) -> totals.merge(model, points, Integer::sum));
                    return totals;
                }),
                data.accuracyByModel(),
                activeModels,
                OVERALL_QUALIFIED_PREDICTIONS
        ));
        for (String competition : data.totalByCompetitionAndModel().keySet()) {
            Map<String, Integer> scoredCounts = data.scoredCountByCompetitionAndModel().getOrDefault(competition, Map.of());
            Map<String, Integer> totals = data.totalByCompetitionAndModel().getOrDefault(competition, Map.of());
            Map<String, Double> avgPoints = new LinkedHashMap<>();
            for (String model : leaderboardModels) {
                int scored = scoredCounts.getOrDefault(model, 0);
                avgPoints.put(model, scored > 0 ? (double) totals.getOrDefault(model, 0) / scored : 0.0);
            }
            stats.put(competition, buildLeaderboardRows(
                    leaderboardModels,
                    scoredCounts,
                    avgPoints,
                    totals,
                    data.accuracyByCompetitionAndModel().getOrDefault(competition, Map.of()),
                    activeModels,
                    COMPETITION_QUALIFIED_PREDICTIONS
            ));
        }
        return stats;
    }

    private Map<String, Object> buildLeaderboardRows(List<String> models,
                                                     Map<String, Integer> scoredCounts,
                                                     Map<String, Double> avgPoints,
                                                     Map<String, Integer> totals,
                                                     Map<String, Map<String, Integer>> accuracy,
                                                     List<String> activeModels,
                                                     int qualifiedPredictions) {
        Map<String, Object> rows = new LinkedHashMap<>();
        for (String model : models) {
            int predicted = scoredCounts.getOrDefault(model, 0);
            double avg = avgPoints.getOrDefault(model, 0.0);
            boolean benchmark = RandomPredictionService.MODEL_NAME.equals(model);
            boolean active = !benchmark && activeModels.contains(model);
            boolean qualified = active && predicted >= qualifiedPredictions;
            double sampleWeight = active ? Math.min(1.0, (double) predicted / qualifiedPredictions) : 0.0;
            double adjusted = avg * sampleWeight;
            Map<String, Integer> modelAccuracy = accuracy.getOrDefault(model, Map.of());
            rows.put(model, Map.of(
                    "predicted", predicted,
                    "avg", avg,
                    "adjusted", adjusted,
                    "total", totals.getOrDefault(model, 0),
                    "exact", modelAccuracy.getOrDefault("exact", 0),
                    "tendency", modelAccuracy.getOrDefault("tendency", 0),
                    "wrong", modelAccuracy.getOrDefault("wrong", 0),
                    "active", active,
                    "qualified", qualified,
                    "status", benchmark ? "benchmark" : active ? (qualified ? "active" : "provisional") : "inactive"
            ));
        }
        return rows;
    }

    private Map<String, Object> buildLineChartsData(DashboardData data) {
        String[] colors = {
                "rgb(74,144,226)", "rgb(80,200,120)",
                "rgb(255,159,64)", "rgb(153,102,255)"
        };
        Map<String, Object> lineCharts = new LinkedHashMap<>();
        for (String comp : data.competitions()) {
            Map<Integer, Map<String, Integer>> cumulative = data.cumulativeByCompetitionAndMatchday().get(comp);
            if (cumulative == null) continue;
            List<Integer> matchdays = new ArrayList<>(cumulative.keySet());
            List<Map<String, Object>> datasets = new ArrayList<>();
            int j = 0;
            for (String m : data.models()) {
                List<Integer> values = matchdays.stream()
                        .map(day -> cumulative.get(day).get(m))
                        .toList();
                datasets.add(Map.of(
                        "label", m,
                        "data", values,
                        "borderColor", colors[j++ % colors.length],
                        "tension", 0.3,
                        "fill", false,
                        "spanGaps", false
                ));
            }
            lineCharts.put(comp, Map.of("labels", matchdays, "datasets", datasets));
        }
        return lineCharts;
    }
}
