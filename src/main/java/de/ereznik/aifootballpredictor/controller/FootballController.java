package de.ereznik.aifootballpredictor.controller;

import de.ereznik.aifootballpredictor.service.FootballPredictionService;
import de.ereznik.aifootballpredictor.service.FootballResultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> getPredictions() {
        new Thread(() -> footballPredictionService.runPredictions()).start();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("result")
    public ResponseEntity<Void> getResults() {
        new Thread(() -> footballResultService.runResults()).start();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}