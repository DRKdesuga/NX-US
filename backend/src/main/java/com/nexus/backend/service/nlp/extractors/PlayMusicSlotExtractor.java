package com.nexus.backend.service.nlp.extractors;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class PlayMusicSlotExtractor {

    private static final Pattern SONG_PATTERN = Pattern.compile(
            "(play|listen to)\\s+(?<song>[\\w\\s']+?)" +
                    "(?:\\s+by\\s+(?<artist>[\\w\\s']+?))?" +
                    "(?=\\s+(on|via|with|using|for|to)\\s+|$)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Extracts song and artist slots from a given input text.
     *
     * @param text Cleaned input text.
     * @return Map of slot names to values.
     */
    public Map<String, String> extract(String text) {

        Map<String, String> slots = new HashMap<>();

        Matcher matcher = SONG_PATTERN.matcher(text);
        if (matcher.find()) {
            String song = matcher.group("song");
            String artist = matcher.group("artist");

            if (song != null && !song.isBlank()) {
                slots.put("song", song.strip());
            }

            if (artist != null && !artist.isBlank()) {
                slots.put("artist", artist.strip());
            }
        }

        return slots;
    }
}
