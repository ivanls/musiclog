package com.ivan.musiclog.controller;

import com.ivan.musiclog.dto.SpotifyAlbumResponse;
import com.ivan.musiclog.dto.SpotifyArtistResponse;
import com.ivan.musiclog.dto.SpotifySearchResponse;
import com.ivan.musiclog.dto.SpotifyTrackResponse;
import com.ivan.musiclog.service.SpotifyService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
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

    @GetMapping("/artists/{id}")
    public SpotifyArtistResponse getArtistById(@PathVariable String id) { return spotifyService.getArtistById(id); }

    @GetMapping("/albums/{id}")
    public SpotifyAlbumResponse getAlbumById(@PathVariable String id) { return spotifyService.getAlbumById(id); }

    @GetMapping("/tracks/{id}")
    public SpotifyTrackResponse getTrackById(@PathVariable String id) { return spotifyService.getTrackById(id); }
}