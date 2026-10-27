package com.ivan.musiclog.repository;

import com.ivan.musiclog.entity.AlbumReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlbumReviewRepository extends JpaRepository<AlbumReview, Long> {


    boolean existsByUser_IdAndAlbum_SpotifyAlbumId(Long userId, String spotifyAlbumId);

    List<AlbumReview> findByAlbum_SpotifyAlbumId(String spotifyAlbumId);

    Page<AlbumReview> findReviewsByUser_IdAndAlbum_SpotifyAlbumIdOrderByListenedAtDesc(Long userId, String spotifyAlbumId, Pageable pageable);

    Page<AlbumReview> findReviewsByUser_IdAndAlbum_Artist_SpotifyArtistIdOrderByListenedAtDesc(Long userId, String spotifyAlbumId, Pageable pageable);

    Page<AlbumReview> findReviewsByUser_IdAndAlbum_Artist_SpotifyArtistIdAndAlbum_SpotifyAlbumIdOrderByListenedAtDesc(
            Long userId,
            String artistId,
            String albumId,
            Pageable pageable
    );

    @Query("SELECT r FROM AlbumReview r WHERE r.user.id = :userId ORDER BY r.listenedAt DESC NULLS LAST")
    Page<AlbumReview> findReviewsByUser_IdOrderByListenedAtDesc(
            @Param("userId") Long userId, Pageable pageable
    );
}