package de.ereznik.aifootballpredictor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class RestClientConfig {

    private final String baseUrl;
    private final String token;

    public RestClientConfig(@Value("${football-data.base-url}") String baseUrl,
                            @Value("${football-data.token}") String token) {
        this.baseUrl = baseUrl;
        this.token = token;
    }

    @Bean
    public RestClient getyRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("X-Auth-Token", token)
                .build();
    }
}