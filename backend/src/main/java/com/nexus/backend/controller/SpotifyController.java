package com.nexus.backend.controller;

import com.nexus.backend.dto.response.SpotifyLoginResponse;
import com.nexus.backend.errors.SpotifyErrors;
import com.nexus.backend.service.spotify.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
