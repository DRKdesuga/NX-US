package com.nexus.backend.controller;

import com.nexus.backend.dto.response.SpotifyLoginResponse;
import com.nexus.backend.errors.SpotifyErrors;
import com.nexus.backend.service.spotify.SpotifyService;
import com.nexus.backend.dto.response.SpotifyTokenResponse;
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

}
