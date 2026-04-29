package de.ereznik.aifootballpredictor.dto.news;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NewsSearchResult(
        String title,
        String url,
        String description,
        @JsonProperty("page_age") String pageAge,
        @JsonProperty("extra_snippets") List<String> extraSnippets
) {
}