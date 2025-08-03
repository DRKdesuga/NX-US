package com.nexus.backend.controller;

import com.nexus.backend.dto.request.GptRequest;
import com.nexus.backend.dto.response.GptResponse;
import com.nexus.backend.errors.GptErrors;
import com.nexus.backend.service.gpt.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gpt")
@RequiredArgsConstructor
public class GptController {

    private final GptService gptService;

    @PostMapping("/ask")
    public ResponseEntity<?> askGpt(@RequestBody GptRequest request) {
        try {
            GptResponse response = gptService.askGpt(request.getPrompt());
            return ResponseEntity.ok(response);
        } catch (GptErrors e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
