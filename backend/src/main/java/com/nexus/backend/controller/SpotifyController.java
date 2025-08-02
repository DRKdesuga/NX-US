package com.nexus.backend.controller;

import com.nexus.backend.dto.response.SpotifyLoginResponse;
import com.nexus.backend.errors.SpotifyErrors;
import com.nexus.backend.service.spotify.SpotifyService;
import com.nexus.backend.dto.response.SpotifyTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nexus.backend.dto.request.SpotifySearchRequest;
import com.nexus.backend.dto.response.SpotifySearchResponse;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import com.nexus.backend.dto.request.SpotifyPlayRequest;
import com.nexus.backend.dto.response.SpotifyPlayResponse;
import com.nexus.backend.dto.response.SpotifyPauseResponse;
import com.nexus.backend.dto.request.SpotifyPlayPlaylistRequest;
import com.nexus.backend.dto.response.SpotifyNextResponse;

@RestController
@RequestMapping("/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;

    @GetMapping("/login")
    public ResponseEntity<?> login() {
        try {
            SpotifyLoginResponse response = spotifyService.generateLoginUrl();
            return ResponseEntity.ok(response);
        } catch (SpotifyErrors e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam(name = "code") String code)
    {

        System.out.println("Received callback with code: " + code);

        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing 'code' parameter");
        }

        try {
            SpotifyTokenResponse response = spotifyService.handleCallback(code);
            return ResponseEntity.ok(response);
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
            SpotifySearchResponse response = spotifyService.searchTrack(request.getQuery(), accessToken);
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


}
