package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.Competition;
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
    public MatchesResponse fetchScheduledMatches(Competition competition, LocalDate from, LocalDate to) {
        return getMatches(competition, Status.SCHEDULED);
    }

    @Override
    public MatchesResponse fetchFinishedMatches(Competition competition, LocalDate from, LocalDate to) {
        return getMatches(competition, Status.FINISHED);
    }

    private MatchesResponse getMatches(Competition competition, Status status) {
        try (var is = getClass().getResourceAsStream("/mock-data/football-response-" + competition.toString().toLowerCase() + "-" + status.toString().toLowerCase() + ".json")) {
            return objectMapper.readValue(is, MatchesResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mock data", e);
        }
    }
}