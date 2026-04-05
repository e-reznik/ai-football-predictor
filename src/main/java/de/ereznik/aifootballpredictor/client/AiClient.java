package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.ai.AiRequest;
import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;

public interface AiClient {
    PredictionResponse retrieveResponseFromModel(AiRequest aiRequest);
}
