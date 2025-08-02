package com.nexus.backend.service.spotify;

import com.nexus.backend.errors.SpotifyErrors;
import com.nexus.backend.dto.response.SpotifyTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Handles the Spotify login URL generation and token exchange for OAuth authentication.
 */
@Component
public class SpotifyLogin {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.redirect.uri}")
    private String redirectUri;

    public String buildLoginURL() throws SpotifyErrors {
        if (clientId == null || redirectUri == null) {
            throw new SpotifyErrors("Spotify clientId or redirectUri not configured");
        }
        String scope = URLEncoder.encode("user-read-private user-read-email user-modify-playback-state", StandardCharsets.UTF_8);
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        return "https://accounts.spotify.com/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + encodedRedirect +
                "&scope=" + scope;
    }

    public SpotifyTokenResponse exchangeCodeForToken(String code) throws SpotifyErrors {
        try {
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://accounts.spotify.com")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .build();

            return webClient.post()
                    .uri("/api/token")
                    .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                            .with("code", code)
                            .with("redirect_uri", redirectUri))
                    .retrieve()
                    .bodyToMono(SpotifyTokenResponse.class)
                    .block();

        } catch (Exception e) {
            throw new SpotifyErrors("Error while exchanging code for token: " + e.getMessage());
        }
    }
}
