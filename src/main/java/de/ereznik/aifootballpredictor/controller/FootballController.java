package de.ereznik.aifootballpredictor.controller;

import de.ereznik.aifootballpredictor.dto.Matches;
import de.ereznik.aifootballpredictor.service.FootballService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FootballController {
    private final FootballService footballService;

    public FootballController(FootballService footballService) {
        this.footballService = footballService;
    }

    @GetMapping("matches")
    public Matches getMatches() {
        return footballService.getMatches();
    }
}
