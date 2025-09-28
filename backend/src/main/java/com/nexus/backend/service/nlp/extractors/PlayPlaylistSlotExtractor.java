package com.nexus.backend.service.nlp.extractors;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PlayPlaylistSlotExtractor {

    private static final Pattern QUOTED =
            Pattern.compile("(?i)\\bplay\\s+\"(?<name>[^\"]+)\"(?:\\s*(?:playlist|play\\s*list|mix))?");

    private static final Pattern AFTER_KEYWORD =
            Pattern.compile("(?i)\\bplay(?:\\s+my)?\\s+(?:playlist|play\\s*list|mix)\\s+(?<name>.+)$");

    private static final Pattern BEFORE_KEYWORD =
            Pattern.compile("(?i)\\bplay\\s+(?<name>.+?)\\s*(?:playlist|play\\s*list|mix)\\b");

    public Map<String, String> extract(String text) {
        Map<String, String> slots = new HashMap<>();
        if (text == null || text.isBlank()) return slots;

        String name = null;

        Matcher m1 = QUOTED.matcher(text);
        if (m1.find()) name = m1.group("name");

        if (name == null) {
            Matcher m2 = AFTER_KEYWORD.matcher(text);
            if (m2.find()) name = m2.group("name");
        }

        if (name == null) {
            Matcher m3 = BEFORE_KEYWORD.matcher(text);
            if (m3.find()) name = m3.group("name");
        }

        if (name != null) {
            name = name.replaceAll("(?i)\\s+(on|via|with|using|for|to)\\s+.*$", "");
            name = name.replaceAll("\\s+", " ").trim();
            if (!name.isEmpty()) slots.put("playlist", name);
        }

        return slots;
    }
}
