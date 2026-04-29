package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.news.NewsSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class NewsClientImpl implements NewsClient {
    private final RestClient restClient;

    public NewsClientImpl(@Qualifier("newsRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public NewsSearchResponse getLatestNews(String query) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/res/v1/web/search")
                        .queryParam("q", query)
                        //.queryParam("country", "ES")
                        //.queryParam("search_lang", "en")
                        .queryParam("count", 5)
                        .queryParam("freshness", "pw")
                        .queryParam("extra_snippets", "true")
                        .queryParam("result_filter", "web,news")
                        .build()
                )
                .retrieve()
                .body(NewsSearchResponse.class);
    }
}