package com.ivan.musiclog.controller;

import com.ivan.musiclog.dto.SpotifySearchResponse;
import com.ivan.musiclog.service.SpotifyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/token")
    public String getToken() {
        return spotifyService.getAccessToken();
    }

    @GetMapping("/search")
    public SpotifySearchResponse search(@RequestParam String q) {
        return spotifyService.search(q);
    }
}