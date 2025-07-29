package com.nexus.backend.service.nlp;

import com.nexus.backend.errors.NLPErrors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntentClassifierTest {

    private final Preprocessor preprocessor = new Preprocessor();
    private final IntentDetector intentDetector = new IntentDetector();

    @Test
    public void testPlayMusicIntent() throws NLPErrors {
        assertEquals("play_music", intentDetector.detect(preprocessor.clean("play the real slim shady")));
        assertEquals("play_music", intentDetector.detect(preprocessor.clean("listen to something cool")));
    }

    @Test
    public void testChatGptIntent() throws NLPErrors {
        assertEquals("chat_gpt_query", intentDetector.detect(preprocessor.clean("tell me a story about dragons")));
        assertEquals("chat_gpt_query", intentDetector.detect(preprocessor.clean("what is love")));
    }

    @Test
    public void testUnknownIntentThrows() {
        String input = "i love you nexus";
        Exception exception = assertThrows(NLPErrors.class, () -> intentDetector.detect(preprocessor.clean(input)));
        assertTrue(exception.getMessage().contains("Unable to classify"));
    }
}
