package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.ai.AiRequest;
import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;
import de.ereznik.aifootballpredictor.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
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
    public PredictionResponse retrieveResponseFromModel(AiRequest aiRequest) {
        log.debug("Retrieving response from chat model: {}", aiRequest.chatModel());
        String modelResponse = aiRequest.chatModel().call(aiRequest.prompt());

        String cleaned = JsonUtils.cleanJson(modelResponse);

        log.debug("Response from {}. Original: {}\nCleaned: {}", aiRequest.chatModel(), modelResponse, cleaned);

        String modelName = aiRequest.chatModel().getDefaultOptions().getModel();
        return objectMapper.readValue(cleaned, PredictionResponse.class)
                .toBuilder().modelName(modelName).competition(aiRequest.competition()).build();
    }
}
