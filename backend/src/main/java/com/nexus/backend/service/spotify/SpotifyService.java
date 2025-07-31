package com.nexus.backend.service.spotify;

import com.nexus.backend.dto.response.SpotifyLoginResponse;
import com.nexus.backend.dto.response.SpotifyTokenResponse;
import com.nexus.backend.errors.SpotifyErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
