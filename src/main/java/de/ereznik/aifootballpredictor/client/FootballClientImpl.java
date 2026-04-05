package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.Competition;
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
    public MatchesResponse fetchScheduledMatches(Competition competition, LocalDate from, LocalDate to) {
        return fetchMatches(competition, from, to, Status.SCHEDULED);
    }

    @Override
    public MatchesResponse fetchFinishedMatches(Competition competition, LocalDate from, LocalDate to) {
        return fetchMatches(competition, from, to, Status.FINISHED);
    }

    private MatchesResponse fetchMatches(Competition competition, LocalDate from, LocalDate to, Status status) {
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
