package com.nexus.backend.service.nlp;

import com.nexus.backend.errors.NLPErrors;
import com.nexus.backend.service.nlp.extractors.ChatGPTSlotExtractor;
import com.nexus.backend.service.nlp.extractors.PlayMusicSlotExtractor;
import com.nexus.backend.service.nlp.extractors.PlayPlaylistSlotExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SlotExtractorTest {

    private Preprocessor preprocessor;
    private IntentDetector intentDetector;
    private SlotExtractor slotExtractor;

    @BeforeEach
    public void setUp() {
        preprocessor = new Preprocessor();
        intentDetector = new IntentDetector();
        PlayMusicSlotExtractor playMusicSlotExtractor = new PlayMusicSlotExtractor();
        ChatGPTSlotExtractor chatGPTSlotExtractor = new ChatGPTSlotExtractor();
        PlayPlaylistSlotExtractor playPlaylistSlotExtractor = new PlayPlaylistSlotExtractor();
        slotExtractor = new SlotExtractor(playMusicSlotExtractor, chatGPTSlotExtractor, playPlaylistSlotExtractor);
    }

    @Test
    public void testPlayMusicExtraction() throws NLPErrors {
        String query = "play The Real Slim Shady by Eminem";
        String intent = intentDetector.detect(preprocessor.clean(query));
        Map<String, String> slots = slotExtractor.extractSlots(intent, query);

        assertEquals("The Real Slim Shady", slots.get("song"));
        assertEquals("Eminem", slots.get("artist"));
    }

    @Test
    public void testPlayMusicExtractionNoisy() throws NLPErrors {
        String query = "Okay I was actually talking to my girfriend and i want you to play The Real Slim Shady by Eminem on Spotify just to make sure you understand me";
        String intent = intentDetector.detect(preprocessor.clean(query));
        Map<String, String> slots = slotExtractor.extractSlots(intent, query);

        assertEquals("The Real Slim Shady", slots.get("song"));
        assertEquals("Eminem", slots.get("artist"));
    }

    @Test
    public void testPlayMusicExtractionWithOnSpotify() throws NLPErrors {
        String query = "play we never dated by Sombr on Spotify";
        String intent = intentDetector.detect(preprocessor.clean(query));
        Map<String, String> slots = slotExtractor.extractSlots(intent, query);

        assertEquals("we never dated", slots.get("song").toLowerCase());
        assertEquals("sombr", slots.get("artist").toLowerCase());
    }

    @Test
    public void testChatGptExtraction() throws NLPErrors {
        String query = "tell me a story about dragons";
        String intent = intentDetector.detect(preprocessor.clean(query));
        Map<String, String> slots = slotExtractor.extractSlots(intent, query);

        assertEquals("a story about dragons", slots.get("prompt"));
    }

    @Test
    public void testChatGptExtractionNoisy() throws NLPErrors {
        String query = "I'm starting to thing about chinese history and stull and I want you to tell me a story about dragons";
        String intent = intentDetector.detect(preprocessor.clean(query));
        Map<String, String> slots = slotExtractor.extractSlots(intent, query);

        assertEquals("a story about dragons", slots.get("prompt"));
    }

    @Test
    public void testChatGptExtractionWithWhatIs() throws NLPErrors {
        String query = "what is quantum computing";
        String intent = intentDetector.detect(preprocessor.clean(query));
        Map<String, String> slots = slotExtractor.extractSlots(intent, query);

        assertEquals("quantum computing", slots.get("prompt"));
    }
}
