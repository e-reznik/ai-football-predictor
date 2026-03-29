package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
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
    public MatchesResponse fetchMatches(LocalDate today, String competition) {
        try (var is = getClass().getResourceAsStream("/mock-data/football-response.json")) {
            return objectMapper.readValue(is, MatchesResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mock data", e);
        }
    }
}