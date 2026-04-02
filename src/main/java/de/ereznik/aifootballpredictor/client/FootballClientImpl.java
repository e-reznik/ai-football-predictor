package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.football.Status;
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
    public MatchesResponse fetchScheduledMatches(LocalDate from, LocalDate to, String competition) {
        return fetchMatches(from, to, competition, Status.SCHEDULED);
    }

    @Override
    public MatchesResponse fetchFinishedMatches(LocalDate from, LocalDate to, String competition) {
        return fetchMatches(from, to, competition, Status.FINISHED);
    }

    private MatchesResponse fetchMatches(LocalDate from, LocalDate to, String competition, Status status) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/competitions/{competition}/matches")
                        .queryParam("dateFrom", from)
                        .queryParam("dateTo", to)
                        .queryParam("status", status)
                        .build(competition))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(MatchesResponse.class);
    }
}
