package de.ereznik.aifootballpredictor.dto;

public record Match(Competition competition, int id, int matchday, Team homeTeam, Team awayTeam, Score score) {
}
