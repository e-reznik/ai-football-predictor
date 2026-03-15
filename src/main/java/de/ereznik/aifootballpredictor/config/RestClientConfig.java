package de.ereznik.aifootballpredictor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class RestClientConfig {

    private final String BASE_URL;
    private final String TOKEN;

    public RestClientConfig(@Value("${football-data.base-url}") String BASE_URL,
                            @Value("${football-data.token}") String TOKEN) {
        this.BASE_URL = BASE_URL;
        this.TOKEN = TOKEN;
    }

    @Bean
    public RestClient myRestClient(RestClient.Builder builder) {
        log.debug("Creating RestClient with token {}", TOKEN);
        return builder
                .baseUrl(BASE_URL)
                .defaultHeader("X-Auth-Token", TOKEN)
                .requestInterceptor((request, body, execution) -> {
                    log.info("URI: {}", request.getURI());
                    return execution.execute(request, body);
                })
                .build();
    }
}