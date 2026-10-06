package com.ivan.musiclog.repository;

import com.ivan.musiclog.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
}