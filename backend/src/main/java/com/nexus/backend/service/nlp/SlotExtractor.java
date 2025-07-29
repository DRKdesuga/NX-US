package com.nexus.backend.service.nlp;

import com.nexus.backend.errors.NLPErrors;
import com.nexus.backend.service.nlp.extractors.ChatGPTSlotExtractor;
import com.nexus.backend.service.nlp.extractors.PlayMusicSlotExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Selects and executes the appropriate slot extractor based on the given intent.
 */
@Component
@RequiredArgsConstructor
public class SlotExtractor {

    private final PlayMusicSlotExtractor playMusicSlotExtractor;
    private final ChatGPTSlotExtractor chatGPTSlotExtractor;

    /**
     * Dispatches the text to the appropriate extractor based on the intent.
     *
     * @param intent The classified intent.
     * @param text   The cleaned input text.
     * @return A map of extracted slot values.
     * @throws NLPErrors if the intent is unknown or unsupported.
     */
    public Map<String, String> extractSlots(String intent, String text) throws NLPErrors {
        return switch (intent) {
            case "play_music" -> playMusicSlotExtractor.extract(text);
            case "chat_gpt_query" -> chatGPTSlotExtractor.extract(text);
            default -> throw new NLPErrors("Unsupported or unknown intent: " + intent);
        };
    }
}
