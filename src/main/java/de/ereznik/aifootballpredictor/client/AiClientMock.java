package de.ereznik.aifootballpredictor.client;

import de.ereznik.aifootballpredictor.dto.ai.AiRequest;
import de.ereznik.aifootballpredictor.dto.ai.PredictionResponse;
import de.ereznik.aifootballpredictor.util.JsonUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Profile("mock")
public class AiClientMock implements AiClient {
    private final ObjectMapper objectMapper;

    public AiClientMock(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public PredictionResponse retrieveResponseFromModel(AiRequest aiRequest) {
        String path = "/mock-data/ai-response-" + aiRequest.chatModel().getClass().getSimpleName() + "-" + aiRequest.competition() + ".json";
        try (InputStream is = getClass().getResourceAsStream(path.toLowerCase())) {

            String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String cleaned = JsonUtils.cleanJson(raw);

            return objectMapper.readValue(cleaned, PredictionResponse.class)
                    .toBuilder().chatModel(aiRequest.chatModel()).build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mock data", e);
        }
    }
}
