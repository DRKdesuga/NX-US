package com.nexus.backend.service.nlp;

import com.nexus.backend.errors.NLPErrors;
import com.nexus.backend.dto.response.NLPResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * NLPService orchestrates text preprocessing, intent detection, and slot extraction.
 */
@Service
@RequiredArgsConstructor
public class NLPService {

    private final Preprocessor preprocessor;
    private final IntentDetector intentClassifier;
    private final SlotExtractor slotExtractor;

    /**
     * Processes raw user input and returns the detected intent and extracted slots.
     *
     * @param rawText Raw user input.
     * @return NLPResponse containing intent, slots, and raw text.
     * @throws NLPErrors if any processing step fails.
     */
    public NLPResponse processText(String rawText) throws NLPErrors {
        String cleaned = preprocessor.clean(rawText);
        String intent = intentClassifier.detect(cleaned);
        Map<String, String> slots = slotExtractor.extractSlots(intent, cleaned);

        return NLPResponse.builder()
                .intent(intent)
                .slots(slots)
                .rawText(rawText)
                .build();
    }
}
