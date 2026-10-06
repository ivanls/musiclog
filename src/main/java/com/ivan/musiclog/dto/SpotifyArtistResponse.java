package com.ivan.musiclog.dto;

public class SpotifyArtistResponse {

    private String id;
    private String name;
    private String imageUrl;
    private String spotifyUrl;

    public SpotifyArtistResponse() {
    }

    public SpotifyArtistResponse(String id, String name, String imageUrl, String spotifyUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.spotifyUrl = spotifyUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSpotifyUrl() {
        return spotifyUrl;
    }
}