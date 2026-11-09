package com.ivan.musiclog.controller;

import com.ivan.musiclog.dto.AlbumListenResponse;
import com.ivan.musiclog.service.AlbumListenService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/album-listens")
public class AlbumListenController {
    private final AlbumListenService albumListenService;

    public AlbumListenController(AlbumListenService albumListenService) {
        this.albumListenService = albumListenService;
    }

    @PostMapping("/{spotifyAlbumId}")
    public AlbumListenResponse setAlbumAsListened (
            @PathVariable String spotifyAlbumId){
        return albumListenService.setAsListened(spotifyAlbumId);
    }

    @PatchMapping("/{spotifyAlbumId}/unlisten")
    public AlbumListenResponse setAlbumAsUnlistened (
            @PathVariable String spotifyAlbumId){
        return albumListenService.unlisten(spotifyAlbumId);
    }

    @GetMapping()
    public AlbumListenResponse getMyListens(){

    }

    @GetMapping("/{spotifyAlbumId}")
    public AlbumListenResponse getListenBySpotifyAlbumId(
            @PathVariable String spotifyAlbumId){
        return  getListenBySpotifyAlbumId(spotifyAlbumId);
    }


}
