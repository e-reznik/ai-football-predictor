package de.ereznik.aifootballpredictor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class RestClientConfig {

    private final String footballDataBaseUrl;
    private final String footballDataToken;
    private final String braveBaseUrl;
    private final String braveToken;

    public RestClientConfig(@Value("${football-data.base-url}") String footballDataBaseUrl,
                            @Value("${football-data.token}") String footballDataToken, @Value("${brave.base_url}") String braveBaseUrl, @Value("${brave.api_key}") String apiKey
    ) {
        this.footballDataBaseUrl = footballDataBaseUrl;
        this.footballDataToken = footballDataToken;
        this.braveBaseUrl = braveBaseUrl;
        this.braveToken = apiKey;
    }

    @Bean("footballRestClient")
    public RestClient footballRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(footballDataBaseUrl)
                .defaultHeader("X-Auth-Token", footballDataToken)
                .build();
    }

    @Bean("newsRestClient")
    public RestClient newsRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(braveBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept-Encoding", "gzip")
                .defaultHeader("X-Subscription-Token", braveToken)
                .build();
    }
}