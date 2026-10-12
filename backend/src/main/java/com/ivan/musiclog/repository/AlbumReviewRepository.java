package com.ivan.musiclog.repository;

import com.ivan.musiclog.dto.CreateAlbumReviewRequest;
import com.ivan.musiclog.entity.AlbumReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumReviewRepository extends JpaRepository<AlbumReview, Long> {
    boolean existsByUser_IdAndSpotifyAlbumId(Long userId, String spotifyAlbumId);
}
