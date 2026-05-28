package de.ereznik.aifootballpredictor.dto.dashboard;

public record ConsensusPredictionView(
        String predictedScore,
        String outcome,
        int averageConfidence,
        int agreeingModels,
        int totalModels
) {
}
