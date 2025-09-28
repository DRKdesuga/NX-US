package com.nexus.backend.service.nlp;

import com.nexus.backend.errors.NLPErrors;
import com.nexus.backend.service.nlp.extractors.ChatGPTSlotExtractor;
import com.nexus.backend.service.nlp.extractors.PlayMusicSlotExtractor;
import com.nexus.backend.service.nlp.extractors.PlayPlaylistSlotExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SlotExtractor {

    private final PlayMusicSlotExtractor playMusicSlotExtractor;
    private final ChatGPTSlotExtractor chatGPTSlotExtractor;
    private final PlayPlaylistSlotExtractor playPlaylistSlotExtractor;

    public Map<String, String> extractSlots(String intent, String text) throws NLPErrors {
        return switch (intent) {
            case "play_music"     -> playMusicSlotExtractor.extract(text);
            case "play_playlist"  -> playPlaylistSlotExtractor.extract(text);
            case "chat_gpt_query" -> chatGPTSlotExtractor.extract(text);
            case "pause_music", "next_track", "previous_track" -> Map.of();
            default -> throw new NLPErrors("Unsupported or unknown intent: " + intent);
        };
    }
}
