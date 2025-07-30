package com.nexus.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO representing the login URL for Spotify.
 */
@Data
@AllArgsConstructor
public class SpotifyLoginResponse {
    private String url;
}
