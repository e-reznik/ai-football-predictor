package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.client.NewsClient;
import de.ereznik.aifootballpredictor.dto.football.Competition;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.news.NewsPerMatchPerCompetition;
import de.ereznik.aifootballpredictor.dto.news.NewsSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class LatestNewsService {

    private final NewsClient newsRestClient;
    private final String suffix;

    public LatestNewsService(NewsClient newsRestClient, @Value("${brave.suffix}") String suffix) {
        this.newsRestClient = newsRestClient;
        this.suffix = suffix;
    }

    public NewsPerMatchPerCompetition getNews(List<MatchesResponse> matchesList) {
        NewsPerMatchPerCompetition newsPerMatchPerCompetitions = new NewsPerMatchPerCompetition(new HashMap<>());

        for (MatchesResponse matches : matchesList) {
            for (MatchesResponse.Match match : matches.matches()) {
                String competitionCode = match.competition().code();
                String homeTeam = match.homeTeam().name();
                String awayTeam = match.awayTeam().name();

                String query = competitionCode + " " + homeTeam + " vs. " + awayTeam + " " + suffix;

                NewsSearchResponse newsSearchResponse = newsRestClient.getLatestNews(query);
                log.debug("News response: {}", newsSearchResponse);

                newsPerMatchPerCompetitions.newsPerMatchPerCompetition().computeIfAbsent(Competition.valueOf(competitionCode), k -> new HashMap<>())
                        .put(match, newsSearchResponse);
            }
        }

        log.debug("News per competition created: {}", newsPerMatchPerCompetitions);
        return newsPerMatchPerCompetitions;
    }
}