package com.ivan.musiclog.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AlbumListenResponse {

    private Long id;
    private String spotifyAlbumId;
    private String albumName;
    private String artistName;
    private String imageUrl;
    private boolean listened;
    private LocalDateTime listenedAt;

    public AlbumListenResponse() {
    }

    public AlbumListenResponse(Long id, String spotifyAlbumId, String albumName, String artistName, String imageUrl, boolean listened, LocalDateTime listenedAt) {
        this.id = id;
        this.spotifyAlbumId = spotifyAlbumId;
        this.albumName = albumName;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
        this.listened = listened;
        this.listenedAt = listenedAt;
    }
}