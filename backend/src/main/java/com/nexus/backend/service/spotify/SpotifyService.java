package com.nexus.backend.service.spotify;

import com.nexus.backend.dto.response.SpotifyLoginResponse;
import com.nexus.backend.dto.response.SpotifyTokenResponse;
import com.nexus.backend.errors.SpotifyErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import com.nexus.backend.dto.response.SpotifySearchResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import com.nexus.backend.dto.response.SpotifyPlayResponse;
import com.nexus.backend.dto.response.SpotifyPauseResponse;

/**
 * Service layer to encapsulate Spotify-related operations.
 */
@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final SpotifyLogin spotifyLogin;

    public SpotifyLoginResponse generateLoginUrl() throws SpotifyErrors {
        String url = spotifyLogin.buildLoginURL();
        return new SpotifyLoginResponse(url);
    }

    public SpotifyTokenResponse handleCallback(String code) throws SpotifyErrors {
        return spotifyLogin.exchangeCodeForToken(code);
    }

    public SpotifySearchResponse searchTrack(String query, String accessToken) throws SpotifyErrors {
        try {
            WebClient client = WebClient.create("https://api.spotify.com");

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

    public SpotifyPlayResponse playUserPlaylistByName(String playlistName, String accessToken) throws SpotifyErrors {
        try {
            WebClient client = WebClient.create("https://api.spotify.com");

            JsonNode playlists = client.get()
                    .uri("/v1/me/playlists?limit=50") // on limite à 50 pour être safe
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode items = playlists.path("items");

            String playlistUri = null;
            for (JsonNode item : items) {
                if (item.path("name").asText().equalsIgnoreCase(playlistName)) {
                    playlistUri = item.path("uri").asText();
                    break;
                }
            }

            if (playlistUri == null) {
                return new SpotifyPlayResponse(false); // Not found
            }

            String body = String.format("{\"context_uri\": \"%s\"}", playlistUri);

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


}
