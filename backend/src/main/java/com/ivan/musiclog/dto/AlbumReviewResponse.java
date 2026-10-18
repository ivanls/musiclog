package com.ivan.musiclog.dto;

import java.time.LocalDateTime;

public class AlbumReviewResponse {

    private Long id;
    private String username;
    private String spotifyAlbumId;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AlbumReviewResponse() {
    }

    public AlbumReviewResponse(Long id, String username, String spotifyAlbumId, Integer rating, String reviewText, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.spotifyAlbumId = spotifyAlbumId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getSpotifyAlbumId() {
        return spotifyAlbumId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
