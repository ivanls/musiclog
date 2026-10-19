package com.ivan.musiclog.repository;

import com.ivan.musiclog.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findBySpotifyArtistId(String spotifyArtistId);

}