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


}
