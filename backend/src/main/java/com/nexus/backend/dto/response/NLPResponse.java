package com.nexus.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Represents the response of the NLP pipeline.
 *
 * Contains the identified intent, extracted slots, and the raw user input.
 */
@Data
@Builder
@AllArgsConstructor
public class NLPResponse {
    private String intent;
    private Map<String, String> slots;
    private String rawText;
}
