package com.nexus.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifySearchResponse {
    private String name;
    private String artist;
    private String url;
    private String uri;
}
