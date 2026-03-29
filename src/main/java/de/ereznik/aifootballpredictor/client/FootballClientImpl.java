package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Service
@Profile("!mock")
public class FootballClientImpl implements FootballClient {
    private final RestClient restClient;

    public FootballClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public MatchesResponse fetchMatches(LocalDate today, String competition) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/competitions/{competition}/matches")
                        .queryParam("dateFrom", today)
                        .queryParam("dateTo", today)
                        .queryParam("status", "SCHEDULED")
                        .build(competition))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(MatchesResponse.class);
    }
}
