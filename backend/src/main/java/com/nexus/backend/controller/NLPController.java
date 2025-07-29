package com.nexus.backend.controller;

import com.nexus.backend.dto.request.NLPRequest;
import com.nexus.backend.dto.response.NLPResponse;
import com.nexus.backend.errors.NLPErrors;
import lombok.RequiredArgsConstructor;
import com.nexus.backend.service.nlp.NLPService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/nlp")
@RequiredArgsConstructor
public class NLPController {

    private final NLPService nlpService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> analyze(@RequestBody NLPRequest request) {
        try {
            NLPResponse response = nlpService.processText(request.getRawText());
            return ResponseEntity.ok(response);
        } catch (NLPErrors e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
