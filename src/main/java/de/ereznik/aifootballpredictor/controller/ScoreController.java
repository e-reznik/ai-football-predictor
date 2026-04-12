package de.ereznik.aifootballpredictor.controller;

import de.ereznik.aifootballpredictor.dto.DashboardData;
import de.ereznik.aifootballpredictor.service.ScoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class ScoreController {
    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
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
        model.addAttribute("barChartData", buildBarChartData(data));
        model.addAttribute("lineChartsData", buildLineChartsData(data));
        model.addAttribute("accuracyChartData", buildAccuracyChartData(data));
        model.addAttribute("upcomingMatches", scoreService.getUpcomingMatches());
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
                List<Integer> values = matchdays.stream().map(day -> cumulative.get(day).getOrDefault(m, 0)).toList();
                datasets.add(Map.of("label", m, "data", values, "borderColor", colors[j++ % colors.length], "tension", 0.3, "fill", false));
            }
            lineCharts.put(comp, Map.of("labels", matchdays, "datasets", datasets));
        }
        return lineCharts;
    }
}
