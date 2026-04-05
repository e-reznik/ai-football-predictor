package de.ereznik.aifootballpredictor.controller;

import de.ereznik.aifootballpredictor.service.FootballPredictionService;
import de.ereznik.aifootballpredictor.service.FootballResultService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FootballController {
    private final FootballPredictionService footballPredictionService;
    private final FootballResultService footballResultService;

    public FootballController(FootballPredictionService footballPredictionService, FootballResultService footballResultService) {

        this.footballPredictionService = footballPredictionService;
        this.footballResultService = footballResultService;
    }

    @GetMapping("predict")
    public void getPredictions() {
        footballPredictionService.runPredictions();
    }

    @GetMapping("result")
    public void getResults() {
        footballResultService.runResults();
    }
}
