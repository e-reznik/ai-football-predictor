package de.ereznik.aifootballpredictor.service;

import de.ereznik.aifootballpredictor.client.NewsClient;
import de.ereznik.aifootballpredictor.dto.football.Competition;
import de.ereznik.aifootballpredictor.dto.football.MatchesResponse;
import de.ereznik.aifootballpredictor.dto.news.NewsPerMatchPerCompetition;
import de.ereznik.aifootballpredictor.dto.news.NewsSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
        Map<Competition, Map<MatchesResponse.Match, NewsSearchResponse>> result = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = matchesList.stream()
                .flatMap(matches -> matches.matches().stream())
                .map(match -> CompletableFuture.runAsync(() -> {
                    String competitionCode = match.competition().code();
                    String query = competitionCode + " " + match.homeTeam().name() + " vs. " + match.awayTeam().name() + " " + suffix;
                    NewsSearchResponse newsSearchResponse = newsRestClient.getLatestNews(query);
                    log.debug("News response: {}", newsSearchResponse);
                    result.computeIfAbsent(Competition.valueOf(competitionCode), k -> new ConcurrentHashMap<>())
                          .put(match, newsSearchResponse);
                }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.debug("News per competition created: {}", result);
        return new NewsPerMatchPerCompetition(result);
    }
}