package com.nexus.backend.controller;

import com.nexus.backend.dto.request.SpotifyPlayPlaylistRequest;
import com.nexus.backend.dto.request.SpotifyPlayRequest;
import com.nexus.backend.dto.request.SpotifySearchRequest;
import com.nexus.backend.dto.response.*;
import com.nexus.backend.errors.SpotifyErrors;
import com.nexus.backend.service.spotify.SpotifyService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @GetMapping("/login")
    public ResponseEntity<?> login() {
        String url = spotifyService.buildAuthorizeUrl();
        return ResponseEntity.ok(java.util.Map.of("url", url));
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing 'code' parameter");
        }
        try {
            SpotifyTokenResponse response = spotifyService.handleCallback(code);
            String token = response.getAccessToken();

            // Escape minimal pour JS inline
            String tokenJs = token.replace("\\", "\\\\").replace("'", "\\'");
            String frontJs = frontendUrl.replace("\\", "\\\\").replace("'", "\\'");

            String html =
                    "<!doctype html><html><head><meta charset='utf-8'><title>Connected</title></head><body>" +
                            "<script>(function(){"
                            + "var t='" + tokenJs + "';"
                            + "try{var o=window.opener||window.parent;if(o&&o!==window){o.postMessage({type:'spotify_token',access_token:t},'*');window.close();return;}}catch(e){}"
                            + "try{var base='" + frontJs + "';var sep=base.indexOf('#')>=0?'':'#';"
                            + "window.location.replace(base+sep+'spotify_token='+encodeURIComponent(t));}catch(e){document.body.textContent='Connected. You can close this window.';}"
                            + "})();</script>"
                            + "</body></html>";

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(html);

        } catch (SpotifyErrors e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/search")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> searchTrack(
            @RequestBody SpotifySearchRequest request,
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String bearerToken) {
        try {
            String accessToken = bearerToken.replace("Bearer ", "");
            SpotifySearchResponse response = spotifyService.searchTrack(
                    request.getTitle(),
                    request.getArtist(),
                    accessToken
            );
            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No track found");
            }
            return ResponseEntity.ok(response);
        } catch (SpotifyErrors e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PostMapping("/play")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> playTrack(
            @RequestBody SpotifyPlayRequest request,
            @RequestHeader("Authorization") String bearerToken) {
        try {
            String accessToken = bearerToken.replace("Bearer ", "");
            SpotifyPlayResponse response = spotifyService.playTrack(request.getUri(), accessToken);

            if (!response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to start playback.");
            }

            return ResponseEntity.ok(response);

        } catch (SpotifyErrors e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/pause")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> pauseTrack(@RequestHeader("Authorization") String bearerToken) {
        try {
            String accessToken = bearerToken.replace("Bearer ", "");
            SpotifyPauseResponse response = spotifyService.pauseTrack(accessToken);

            if (!response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to pause playback");
            }

            return ResponseEntity.ok(response);

        } catch (SpotifyErrors e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/playplaylist")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> playPlaylist(@RequestBody SpotifyPlayPlaylistRequest request,
                                          @RequestHeader("Authorization") String bearerToken) {
        try {
            String accessToken = bearerToken.replace("Bearer ", "");
            SpotifyPlayResponse response = spotifyService.playUserPlaylistByName(request.getName(), accessToken);

            if (!response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Playlist not found or failed to play.");
            }

            return ResponseEntity.ok(response);

        } catch (SpotifyErrors e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/next")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> nextTrack(@RequestHeader("Authorization") String bearerToken) {
        try {
            String accessToken = bearerToken.replace("Bearer ", "");
            SpotifyNextResponse response = spotifyService.nextTrack(accessToken);

            if (!response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to skip to next track");
            }

            return ResponseEntity.ok(response);
        } catch (SpotifyErrors e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/previous")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> previousTrack(@RequestHeader("Authorization") String bearerToken) {
        try {
            String accessToken = bearerToken.replace("Bearer ", "");
            SpotifyPreviousResponse response = spotifyService.previousTrack(accessToken);

            if (!response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to skip to previous track");
            }

            return ResponseEntity.ok(response);
        } catch (SpotifyErrors e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
