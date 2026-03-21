package de.ereznik.aifootballpredictor.controller;

import de.ereznik.aifootballpredictor.dto.ml.PredictionResponse;
import de.ereznik.aifootballpredictor.service.FootballService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FootballController {
    private final FootballService footballService;

    public FootballController(FootballService footballService) {
        this.footballService = footballService;
    }

    @GetMapping("predict")
    public Map<String, PredictionResponse> getPredictions() {
        return footballService.runPredictions();
    }
}
