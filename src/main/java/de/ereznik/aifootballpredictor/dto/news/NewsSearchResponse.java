package de.ereznik.aifootballpredictor.dto.news;

public record NewsSearchResponse(
        NewsResultList web,
        NewsResultList news
) {
}