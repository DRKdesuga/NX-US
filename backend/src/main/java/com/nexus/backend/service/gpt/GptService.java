package com.nexus.backend.service.gpt;

import com.fasterxml.jackson.databind.JsonNode;
import com.nexus.backend.dto.response.GptResponse;
import com.nexus.backend.errors.GptErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GptService {

    @Value("${gpt.key}")
    private String gptKey;

    public GptResponse askGpt(String prompt) throws GptErrors {
        try {
            WebClient client = WebClient.create("https://api.openai.com");

            JsonNode response = client.post()
                    .uri("/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + gptKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue("{\"model\": \"gpt-4\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            String answer = response
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return new GptResponse(answer);

        } catch (Exception e) {
            throw new GptErrors("Failed to get response from GPT: " + e.getMessage());
        }
    }
}
