package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.ml.PredictionResponse;
import org.springframework.ai.chat.model.ChatModel;

public interface AiClient {
    PredictionResponse retrieveResponseFromModel(ChatModel chatModel, String prompt);
}
