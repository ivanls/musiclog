
package com.ivan.musiclog.dto;

import java.time.LocalDateTime;

public class AlbumReviewResponse {

    private Long id;
    private String username;
    private String spotifyAlbumId;
    private boolean listened;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime listenedAt;

    public AlbumReviewResponse() {
    }

    public AlbumReviewResponse(
            Long id,
            String username,
            String spotifyAlbumId,
            boolean listened,
            Integer rating,
            String reviewText,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime listenedAt
    ) {
        this.id = id;
        this.username = username;
        this.spotifyAlbumId = spotifyAlbumId;
        this.listened = listened;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.listenedAt = listenedAt;
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

    public boolean isListened() {
        return listened;
    }

    public Integer getRating() { return rating; }

    public String getReviewText() {
        return reviewText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getListenedAt() { return listenedAt; }
}
