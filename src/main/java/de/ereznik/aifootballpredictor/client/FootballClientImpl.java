package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.football.Status;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Service
@Profile("!mock")
public class FootballClientImpl implements FootballClient {
    private final RestClient restClient;

    public FootballClientImpl(@Qualifier("footballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public MatchesResponse fetchScheduledMatches(LocalDate from, LocalDate to) {
        return fetchMatches(from, to, Status.SCHEDULED);
    }

    @Override
    public MatchesResponse fetchFinishedMatches(LocalDate from, LocalDate to) {
        return fetchMatches(from, to, Status.FINISHED);
    }

    private MatchesResponse fetchMatches(LocalDate from, LocalDate to, Status status) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/matches")
                        .queryParam("dateFrom", from)
                        .queryParam("dateTo", to)
                        .queryParam("status", status)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(MatchesResponse.class);
    }
}