package com.nexus.backend.service.nlp;

import com.nexus.backend.errors.NLPErrors;
import org.springframework.stereotype.Component;

@Component
public class IntentDetector {

    /**
     * @brief Detects the user's intent based on simple rule-based logic.
     *
     * @param text Cleaned user input.
     * @return A string representing the predicted intent.
     * @throws NLPErrors if no intent matches.
     */
    public String detect(String text) throws NLPErrors {

        if (text == null || text.trim().isEmpty()) {
            throw new NLPErrors("Input text is null or empty.");
        }
        if (text.contains("play") || text.contains("music") || text.contains("listen")) {
            return "play_music";
        }

        if (text.contains("chat") || text.contains("gpt") || text.contains("tell me") || text.startsWith("what is")) {
            return "chat_gpt_query";
        }

        throw new NLPErrors("Unable to classify the intent.");
    }
}