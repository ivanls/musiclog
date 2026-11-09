package com.ivan.musiclog.repository;

import com.ivan.musiclog.entity.AlbumReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumReviewRepository extends JpaRepository<AlbumReview, Long> {

    List<AlbumReview> findByAlbumListen_Album_SpotifyAlbumId(String spotifyAlbumId);

    Page<AlbumReview> findByAlbumListen_User_IdAndAlbumListen_Album_SpotifyAlbumIdOrderByAlbumListen_ListenedAtDesc(
            Long userId,
            String albumId,
            Pageable pageable
    );

    Page<AlbumReview> findByAlbumListen_User_IdAndAlbumListen_Album_Artist_SpotifyArtistIdOrderByAlbumListen_ListenedAtDesc(
            Long userId,
            String artistId,
            Pageable pageable
    );

    Page<AlbumReview> findByAlbumListen_User_IdAndAlbumListen_Album_Artist_SpotifyArtistIdAndAlbumListen_Album_SpotifyAlbumIdOrderByAlbumListen_ListenedAtDesc(
            Long userId,
            String artistId,
            String albumId,
            Pageable pageable
    );

    Page<AlbumReview> findByAlbumListen_User_IdOrderByAlbumListen_ListenedAtDesc(
            Long userId,
            Pageable pageable
    );
}