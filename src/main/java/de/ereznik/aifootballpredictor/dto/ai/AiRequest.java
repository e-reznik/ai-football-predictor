package de.ereznik.aifootballpredictor.dto.ai;

import de.ereznik.aifootballpredictor.dto.football.Competition;
import org.springframework.ai.chat.model.ChatModel;

public record AiRequest(ChatModel chatModel, Competition competition, String prompt) {
}
