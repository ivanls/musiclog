package com.ivan.musiclog.service;

import com.ivan.musiclog.dto.SpotifyAlbumResponse;
import com.ivan.musiclog.dto.SpotifyArtistResponse;
import com.ivan.musiclog.entity.Album;
import com.ivan.musiclog.entity.Artist;
import com.ivan.musiclog.repository.AlbumRepository;
import com.ivan.musiclog.repository.ArtistRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final SpotifyService spotifyService;

    public AlbumService(AlbumRepository albumRepository, ArtistRepository artistRepository, SpotifyService spotifyService) {
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.spotifyService = spotifyService;
    }

    @Transactional
    public Album findOrCreateAlbum(String spotifyAlbumId) {
        Optional<Album> albumOptional =
                albumRepository.findBySpotifyAlbumId(spotifyAlbumId);

        if (albumOptional.isPresent()) {
            return albumOptional.get();
        }

        // Si el album no está guardado, petición a Spoti
        SpotifyAlbumResponse spotifyResponse =
                spotifyService.getAlbumById(spotifyAlbumId);

        // Buscar si está guardado el artista
        Optional<Artist> artistOptional =
                artistRepository.findBySpotifyArtistId(
                        spotifyResponse.getArtistId()
                );

        Artist artist;

        if (artistOptional.isPresent()) {
            // Reutilizamos el artista existente
            artist = artistOptional.get();
        } else {
            // El artista tampoco existe: lo obtenemos de Spotify
            SpotifyArtistResponse artistResponse =
                    spotifyService.getArtistById(
                            spotifyResponse.getArtistId()
                    );

            artist = new Artist();
            artist.setSpotifyArtistId(artistResponse.getId());
            artist.setName(artistResponse.getName());
            artist.setImageUrl(artistResponse.getImageUrl());

            artist = artistRepository.save(artist);
        }

        // Creamos y guardamos el álbum asociado al artista
        Album album = new Album();
        album.setSpotifyAlbumId(spotifyResponse.getId());
        album.setName(spotifyResponse.getName());
        album.setImageUrl(spotifyResponse.getImageUrl());
        album.setSpotifyUrl(spotifyResponse.getSpotifyUrl());
        album.setArtist(artist);

        return albumRepository.save(album);
    }
}
