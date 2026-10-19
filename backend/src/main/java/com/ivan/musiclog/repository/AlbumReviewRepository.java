package com.ivan.musiclog.repository;

import com.ivan.musiclog.entity.AlbumReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumReviewRepository extends JpaRepository<AlbumReview, Long> {

    boolean existsByUser_IdAndAlbum_SpotifyAlbumId(Long userId, String spotifyAlbumId);

    List<AlbumReview> findByAlbum_SpotifyAlbumId(String spotifyAlbumId);

    List<AlbumReview> findReviewsByUser_Id(Long userId);
}