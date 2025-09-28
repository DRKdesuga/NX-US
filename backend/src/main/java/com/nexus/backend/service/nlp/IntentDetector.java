package com.nexus.backend.service.nlp;

import com.nexus.backend.errors.NLPErrors;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class IntentDetector {

    private static final Pattern P_PLAY      = Pattern.compile("\\b(play|listen)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_PLAYLIST  = Pattern.compile("\\b(play\\s*list|playlist|mix)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_PAUSE     = Pattern.compile("\\b(pause|stop|hold on|hold up)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_NEXT      = Pattern.compile("\\b(next|skip|skip track|next song)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_PREV      = Pattern.compile("\\b(previous|prev|back|previous song)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_CHAT      = Pattern.compile("\\b(chat|gpt|tell me|what is|who is|how do|explain)\\b", Pattern.CASE_INSENSITIVE);

    public String detect(String text) throws NLPErrors {
        if (text == null || text.trim().isEmpty()) {
            throw new NLPErrors("Input text is null or empty.");
        }

        if (P_PAUSE.matcher(text).find()) return "pause_music";
        if (P_NEXT.matcher(text).find())  return "next_track";
        if (P_PREV.matcher(text).find())  return "previous_track";

        boolean mentionsPlay = P_PLAY.matcher(text).find();
        boolean mentionsPlaylist = P_PLAYLIST.matcher(text).find();
        if (mentionsPlay && mentionsPlaylist) return "play_playlist";
        if (mentionsPlay) return "play_music";

        if (P_CHAT.matcher(text).find()) return "chat_gpt_query";

        throw new NLPErrors("Unable to classify the intent.");
    }
}
