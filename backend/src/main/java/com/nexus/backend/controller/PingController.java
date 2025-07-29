package com.nexus.backend.controller;

import com.nexus.backend.dto.response.PingResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ping")
public class PingController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PingResponse> ping() {
        return ResponseEntity.ok(new PingResponse("Server is running"));
    }
}
