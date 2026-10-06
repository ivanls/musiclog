package com.ivan.musiclog.dto;

public class SpotifyAlbumResponse {

    private String id;
    private String name;
    private String artistName;
    private String imageUrl;
    private String spotifyUrl;

    public SpotifyAlbumResponse() {
    }

    public SpotifyAlbumResponse(String id, String name, String artistName, String imageUrl, String spotifyUrl) {
        this.id = id;
        this.name = name;
        this.artistName = artistName;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSpotifyUrl() {
        return spotifyUrl;
    }


}
