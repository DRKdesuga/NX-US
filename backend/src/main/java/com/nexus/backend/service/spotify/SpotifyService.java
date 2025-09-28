package com.nexus.backend.service.spotify;

import com.nexus.backend.dto.response.SpotifyLoginResponse;
import com.nexus.backend.dto.response.SpotifyTokenResponse;
import com.nexus.backend.errors.SpotifyErrors;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import com.nexus.backend.dto.response.SpotifySearchResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import com.nexus.backend.dto.response.SpotifyPlayResponse;
import com.nexus.backend.dto.response.SpotifyPauseResponse;
import com.nexus.backend.dto.response.SpotifyNextResponse;
import com.nexus.backend.dto.response.SpotifyPreviousResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Service layer to encapsulate Spotify-related operations.
 */
@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final SpotifyLogin spotifyLogin;

    private static final String AUTH_URL  = "https://accounts.spotify.com/authorize";

    @Value("${spotify.client.id:${spotify.client-id:}}")
    private String clientId;

    @Value("${spotify.client.secret:${spotify.client-secret:}}")
    private String clientSecret;

    @Value("${spotify.redirect.uri:${spotify.redirect-uri:}}")
    private String redirectUri;


    @PostConstruct
    void checkProps() {
        if (isBlank(clientId) || isBlank(clientSecret) || isBlank(redirectUri)) {
            throw new IllegalStateException(
                    "Missing Spotify properties. Expected keys: " +
                            "'spotify.client.id', 'spotify.client.secret', 'spotify.redirect.uri' " +
                            "(or dash versions). Current redirectUri='" + redirectUri + "'."
            );
        }
    }

    public String buildAuthorizeUrl() {
        String scopes = String.join(" ", List.of(
                "user-read-private",
                "user-read-email",
                "playlist-read-private",
                "user-modify-playback-state",
                "user-read-playback-state"
        ));

        return AUTH_URL
                + "?response_type=code"
                + "&client_id="    + enc(clientId)
                + "&redirect_uri=" + enc(redirectUri)
                + "&scope="        + enc(scopes)
                + "&state="        + enc(UUID.randomUUID().toString());
    }


    private static String enc(String v) { return URLEncoder.encode(v, StandardCharsets.UTF_8); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public SpotifyLoginResponse generateLoginUrl() throws SpotifyErrors {
        String url = spotifyLogin.buildLoginURL();
        return new SpotifyLoginResponse(url);
    }

    public SpotifyTokenResponse handleCallback(String code) throws SpotifyErrors {
        return spotifyLogin.exchangeCodeForToken(code);
    }

    public SpotifySearchResponse searchTrack(String title, String artist, String accessToken) throws SpotifyErrors {
        try {
            WebClient client = WebClient.create("https://api.spotify.com");

            StringBuilder queryBuilder = new StringBuilder();
            if (title != null && !title.isEmpty()) {
                queryBuilder.append("track:").append(title).append(" ");
            }
            if (artist != null && !artist.isEmpty()) {
                queryBuilder.append("artist:").append(artist);
            }

            String query = queryBuilder.toString().trim();

            return client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search")
                            .queryParam("q", query)
                            .queryParam("type", "track")
                            .queryParam("limit", 1)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(json -> {
                        JsonNode track = json.path("tracks").path("items").get(0);
                        if (track == null || track.isMissingNode()) {
                            return null;
                        }
                        return new SpotifySearchResponse(
                                track.path("name").asText(),
                                track.path("artists").get(0).path("name").asText(),
                                track.path("external_urls").path("spotify").asText(),
                                track.path("uri").asText()
                        );
                    })
                    .block();

        } catch (Exception e) {
            throw new SpotifyErrors("Failed to search track: " + e.getMessage());
        }
    }


    public SpotifyPlayResponse playTrack(String uri, String accessToken) throws SpotifyErrors {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl("https://api.spotify.com")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            String body = "{\"uris\": [\"" + uri + "\"]}";

            HttpStatusCode status = client.put()
                    .uri("/v1/me/player/play")
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .map(ResponseEntity::getStatusCode)
                    .block();

            return new SpotifyPlayResponse(status != null && status.is2xxSuccessful());

        } catch (Exception e) {
            throw new SpotifyErrors("Failed to play track: " + e.getMessage());
        }
    }

    public SpotifyPauseResponse pauseTrack(String accessToken) throws SpotifyErrors {
        try {
            WebClient client = WebClient.create("https://api.spotify.com");

            HttpStatusCode statusCode = client.put()
                    .uri("/v1/me/player/pause")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity()
                    .map(ResponseEntity::getStatusCode)
                    .block();

            boolean success = statusCode != null && statusCode.is2xxSuccessful();
            return new SpotifyPauseResponse(success);

        } catch (Exception e) {
            throw new SpotifyErrors("Failed to pause track: " + e.getMessage());
        }
    }
    private double similarity(String a, String b) {
        a = a.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        b = b.toLowerCase().replaceAll("[^a-z0-9 ]", "");

        String[] wordsA = a.split("\\s+");
        String[] wordsB = b.split("\\s+");

        int common = 0;
        for (String wordA : wordsA) {
            for (String wordB : wordsB) {
                if (wordA.equals(wordB)) {
                    common++;
                    break;
                }
            }
        }

        int total = Math.max(wordsA.length, wordsB.length);
        return total == 0 ? 1.0 : (double) common / total;
    }


    public SpotifyPlayResponse playUserPlaylistByName(String playlistName, String accessToken) throws SpotifyErrors {
        try {
            WebClient client = WebClient.create("https://api.spotify.com");

            JsonNode playlists = client.get()
                    .uri("/v1/me/playlists?limit=50")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode items = playlists.path("items");
            if (items == null || !items.isArray()) {
                throw new SpotifyErrors("Invalid response from Spotify");
            }

            String bestUri = null;
            double bestScore = 0.0;

            for (JsonNode item : items) {
                String name = item.path("name").asText();
                String uri = item.path("uri").asText();

                double score = similarity(name, playlistName);
                if (score == 1.0) {
                    bestUri = uri;
                    break;
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestUri = uri;
                }
            }

            if (bestUri == null) {
                return new SpotifyPlayResponse(false);
            }

            String body = String.format("{\"context_uri\": \"%s\"}", bestUri);

            HttpStatusCode statusCode = client.put()
                    .uri("/v1/me/player/play")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .map(ResponseEntity::getStatusCode)
                    .block();

            boolean success = statusCode != null && statusCode.is2xxSuccessful();
            return new SpotifyPlayResponse(success);

        } catch (Exception e) {
            throw new SpotifyErrors("Failed to play playlist: " + e.getMessage());
        }
    }

    public SpotifyNextResponse nextTrack(String accessToken) throws SpotifyErrors {
        try {
            WebClient client = WebClient.create("https://api.spotify.com");

            HttpStatusCode statusCode = client.post()
                    .uri("/v1/me/player/next")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toBodilessEntity()
                    .map(ResponseEntity::getStatusCode)
                    .block();

            boolean success = statusCode != null && statusCode.is2xxSuccessful();
            return new SpotifyNextResponse(success);
        } catch (Exception e) {
            throw new SpotifyErrors("Failed to skip to next track: " + e.getMessage());
        }
    }

    public SpotifyPreviousResponse previousTrack(String accessToken) throws SpotifyErrors {
        try {
            WebClient client = WebClient.create("https://api.spotify.com");

            HttpStatusCode statusCode = client.post()
                    .uri("/v1/me/player/previous")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .toBodilessEntity()
                    .map(ResponseEntity::getStatusCode)
                    .block();

            boolean success = statusCode != null && statusCode.is2xxSuccessful();
            return new SpotifyPreviousResponse(success);
        } catch (Exception e) {
            throw new SpotifyErrors("Failed to skip to previous track: " + e.getMessage());
        }
    }



}
