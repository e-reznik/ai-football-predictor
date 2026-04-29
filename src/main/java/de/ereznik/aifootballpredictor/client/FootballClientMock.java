package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.football.Status;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDate;

@Service
@Profile("mock")
public class FootballClientMock implements FootballClient {
    private final ObjectMapper objectMapper;

    public FootballClientMock(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public MatchesResponse fetchScheduledMatches(LocalDate from, LocalDate to) {
        return getMatches(Status.SCHEDULED);
    }

    @Override
    public MatchesResponse fetchFinishedMatches(LocalDate from, LocalDate to) {
        return getMatches(Status.FINISHED);
    }

    private MatchesResponse getMatches(Status status) {
        var allMatches = new java.util.ArrayList<MatchesResponse.Match>();
        for (var competition : de.ereznik.aifootballpredictor.dto.football.Competition.values()) {
            var resourcePath = "/mock-data/football-response-" + competition.toString().toLowerCase() + "-" + status.toString().toLowerCase() + ".json";
            try (var is = getClass().getResourceAsStream(resourcePath)) {
                if (is != null) {
                    allMatches.addAll(objectMapper.readValue(is, MatchesResponse.class).matches());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load mock data for " + competition, e);
            }
        }
        return new MatchesResponse(allMatches);
    }
}