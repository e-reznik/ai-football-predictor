package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.dto.Matches;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Slf4j
@Service
public class FootballService {
    private static final String COMPETITION = "BL1";
    private final RestClient restClient;

    public FootballService(RestClient restClient) {
        this.restClient = restClient;
    }

    public Matches getMatches() {
        LocalDate today = LocalDate.now();
        log.debug("Getting matches for {} for: {}", COMPETITION, today);

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/competitions/{competition}/matches")
                        .queryParam("dateFrom", today)
                        .queryParam("dateTo", today)
                        .build(COMPETITION))
                .retrieve()
                .body(Matches.class);
    }
}
