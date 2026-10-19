package com.ivan.musiclog.repository;

import com.ivan.musiclog.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findBySpotifyAlbumId(String spotifyAlbumId);
}