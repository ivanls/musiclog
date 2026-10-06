package com.ivan.musiclog.dto;

public class SpotifyTrackResponse {

    private String id;
    private String name;
    private String artistName;
    private String albumName;
    private Integer durationMs;
    private String imageUrl;
    private String spotifyUrl;

    public SpotifyTrackResponse() {
    }

    public SpotifyTrackResponse(String id, String name, String artistName, String albumName, Integer durationMs, String imageUrl, String spotifyUrl) {
        this.id = id;
        this.name = name;
        this.artistName = artistName;
        this.albumName = albumName;
        this.durationMs = durationMs;
        this.imageUrl = imageUrl;
        this.spotifyUrl = spotifyUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSpotifyUrl() {
        return spotifyUrl;
    }





}
