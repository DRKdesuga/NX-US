package com.nexus.backend.service.nlp;

import com.nexus.backend.errors.NLPErrors;
import org.springframework.stereotype.Component;


@Component
public class Preprocessor {

    /**
     * @brief Cleans the input text before intent and slot parsing.
     *
     * @param text The raw user input.
     * @return A cleaned, normalized string.
     *
     * @throws NLPErrors if the input is null, empty, or results in unusable content after cleaning.
     */
    public String clean(String text) throws NLPErrors {
        if (text == null || text.trim().isEmpty()) {
            throw new NLPErrors("Input text is null or empty.");
        }

        text = text.toLowerCase();
        text = text.replaceAll("[^\\w\\s]", "");
        text = text.replaceAll("\\s+", " ").trim();

        if (text.isEmpty()) {
            throw new NLPErrors("Input is invalid after preprocessing.");
        }

        return text;
    }
}
