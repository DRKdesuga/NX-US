package com.nexus.backend.service.nlp.extractors;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChatGPTSlotExtractor {

    private static final Pattern PROMPT_PATTERN = Pattern.compile(
            "^(tell me|what is|explain|define)\\s+(?<prompt>.+)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Extracts a prompt slot from the given input text.
     *
     * @param text Cleaned input text.
     * @return Map of slot names to values.
     */
    public Map<String, String> extract(String text) {
        Map<String, String> slots = new HashMap<>();

        Matcher matcher = PROMPT_PATTERN.matcher(text);
        if (matcher.find()) {
            String prompt = matcher.group("prompt");
            if (prompt != null && !prompt.isBlank()) {
                slots.put("prompt", prompt.strip());
            }
        }

        return slots;
    }
}
