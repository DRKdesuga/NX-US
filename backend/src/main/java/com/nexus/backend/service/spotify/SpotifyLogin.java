package com.nexus.backend.service.spotify;

import com.nexus.backend.errors.SpotifyErrors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles the Spotify login URL generation for OAuth authentication.
 */
@Component
public class SpotifyLogin {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.redirect.uri}")
    private String redirectUri;

    public String buildLoginURL() throws SpotifyErrors {
        if (clientId == null || redirectUri == null) {
            throw new SpotifyErrors("Spotify clientId or redirectUri not configured");
        }

        String scope = URLEncoder.encode("user-read-private user-read-email", StandardCharsets.UTF_8);
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        return "https://accounts.spotify.com/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + encodedRedirect +
                "&scope=" + scope;
    }
}
