package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.news.NewsSearchResponse;

public interface NewsClient {
    NewsSearchResponse getLatestNews(String query);
}