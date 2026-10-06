package com.ivan.musiclog.dto;

import java.util.List;

public class SpotifySearchResponse {

    private List<SpotifyAlbumResponse> albums;
    private List<SpotifyArtistResponse> artists;
    private List<SpotifyTrackResponse> tracks;

    public SpotifySearchResponse() {
    }

    public SpotifySearchResponse(List<SpotifyAlbumResponse> albums, List<SpotifyArtistResponse> artists, List<SpotifyTrackResponse> tracks) {
        this.albums = albums;
        this.artists = artists;
        this.tracks = tracks;
    }

    public List<SpotifyAlbumResponse> getAlbums() {
        return albums;
    }

    public List<SpotifyArtistResponse> getArtists() {
        return artists;
    }

    public List<SpotifyTrackResponse> getTracks() {
        return tracks;
    }
}
