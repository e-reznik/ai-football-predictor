package de.ereznik.aifootballpredictor.dto.news;

import java.util.List;

public record NewsResultList(
        List<NewsSearchResult> results
) {
}