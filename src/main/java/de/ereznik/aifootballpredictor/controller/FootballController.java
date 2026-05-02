package de.ereznik.aifootballpredictor.controller;

import de.ereznik.aifootballpredictor.service.FootballPredictionService;
import de.ereznik.aifootballpredictor.service.FootballResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class FootballController {
    private final FootballPredictionService footballPredictionService;
    private final FootballResultService footballResultService;

    public FootballController(FootballPredictionService footballPredictionService, FootballResultService footballResultService) {
        this.footballPredictionService = footballPredictionService;
        this.footballResultService = footballResultService;
    }

    @GetMapping("predict")
    public ResponseEntity<Void> getPredictions() {
        CompletableFuture.runAsync(footballPredictionService::runPredictions);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("result")
    public ResponseEntity<Void> getResults() {
        CompletableFuture.runAsync(footballResultService::runResults);
        return ResponseEntity.accepted().build();
    }
}
