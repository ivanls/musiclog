package com.ivan.musiclog.repository;

import com.ivan.musiclog.entity.AlbumListen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumListenRepository extends JpaRepository <AlbumListen, Long> {
    Optional<AlbumListen> findByUser_IdAndAlbum_SpotifyAlbumId(Long userId, String spotifyAlbumId);}
