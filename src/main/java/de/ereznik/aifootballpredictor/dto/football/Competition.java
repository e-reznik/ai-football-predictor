package de.ereznik.aifootballpredictor.dto.football;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Competition {
    // Leagues
    BL1("Bundesliga"), // Germany
    PL("Premier League"), // England
    SA("Serie A"), // Italy
    PD("Primera Division"), // Spain
    FL1("Ligue 1"), // France
    PPL("Primeira Liga"), // Portugal
    DED("Eredivisie"), // Nederland
    BSA("Série A"), // Brazil
    ELC("Championship"), // England-2
    // Cups
    CL("Champions League"),
    WC("World Cup"),
    EC("European Championship");
    private final String displayName;
}