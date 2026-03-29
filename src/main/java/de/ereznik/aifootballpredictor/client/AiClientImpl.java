package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.ml.PredictionResponse;
import de.ereznik.aifootballpredictor.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@Profile("!mock")
public class AiClientImpl implements AiClient {

    private final ObjectMapper objectMapper;

    public AiClientImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public PredictionResponse retrieveResponseFromModel(ChatModel chatModel, String prompt) {
        log.debug("Retrieving response from chat model: {}", chatModel);
        String modelResponse = chatModel.call(prompt);

        String cleaned = JsonUtils.cleanJson(modelResponse);

        log.debug("Response from {}. Original: {}\nCleaned: {}", chatModel, modelResponse, cleaned);

        return objectMapper.readValue(cleaned, PredictionResponse.class);
    }
}
