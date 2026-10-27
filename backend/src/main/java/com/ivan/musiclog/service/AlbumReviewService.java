package com.ivan.musiclog.service;

import com.ivan.musiclog.dto.AlbumReviewResponse;
import com.ivan.musiclog.dto.CreateAlbumReviewRequest;
import com.ivan.musiclog.dto.UpdateAlbumReviewRequest;
import com.ivan.musiclog.entity.Album;
import com.ivan.musiclog.entity.AlbumReview;
import com.ivan.musiclog.entity.User;
import com.ivan.musiclog.exception.AccessDeniedException;
import com.ivan.musiclog.exception.AlbumReviewNotFoundException;
import com.ivan.musiclog.exception.DuplicateAlbumReviewException;
import com.ivan.musiclog.repository.AlbumRepository;
import com.ivan.musiclog.repository.AlbumReviewRepository;
import com.ivan.musiclog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.ivan.musiclog.entity.Artist;
import com.ivan.musiclog.repository.ArtistRepository;
import com.ivan.musiclog.dto.SpotifyAlbumResponse;
import com.ivan.musiclog.dto.SpotifyArtistResponse;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.List;

@Service
public class AlbumReviewService {

    private final AlbumReviewRepository albumReviewRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final SpotifyService spotifyService;

    public AlbumReviewService(
            AlbumReviewRepository albumReviewRepository,
            UserRepository userRepository,
            AlbumRepository albumRepository,
            ArtistRepository artistRepository,
            SpotifyService spotifyService
    ) {
        this.albumReviewRepository = albumReviewRepository;
        this.userRepository = userRepository;
        this.albumRepository = albumRepository;
        this.spotifyService = spotifyService;
        this.artistRepository = artistRepository;
    }

    public AlbumReviewResponse createAlbumReview(CreateAlbumReviewRequest request) {
        validateReview(request.getRating(), request.getReviewText());

        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        String albumId = request.getSpotifyAlbumId();

        Album album = findOrCreateAlbum(albumId);

        AlbumReview entry = new AlbumReview();
        entry.setUser(user);
        entry.setAlbum(album);
        entry.setListened(request.isListened());
        entry.setRating(request.getRating());
        entry.setReviewText(request.getReviewText());
        entry.setListenedAt(
                request.getListenedAt() == null ? LocalDateTime.now() : request.getListenedAt()
        );

        AlbumReview savedEntry = albumReviewRepository.save(entry);

        return toResponse(savedEntry);
    }

    public AlbumReviewResponse updateAlbumReview(
            Long reviewId,
            UpdateAlbumReviewRequest request
    ) {

        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        AlbumReview review = albumReviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new AlbumReviewNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "No puedes editar esta entrada");
        }

        Integer finalRating = (request.getRating() == null
                ? review.getRating()
                : request.getRating());
        String finalReviewText = (request.getReviewText() == null
                ? review.getReviewText()
                : request.getReviewText());

        validateReview(finalRating, finalReviewText);

        if (request.getListened() != null) {
            review.setListened(request.getListened());
        }
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getReviewText() != null) {
            review.setReviewText(request.getReviewText());
        }
        if (request.getListenedAt() != null) {
            review.setListenedAt(request.getListenedAt());
        }

        AlbumReview updatedReview = albumReviewRepository.save(review);

        return toResponse(updatedReview);
    }

    public AlbumReviewResponse getReviewById(Long reviewId) {
        AlbumReview review = albumReviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new AlbumReviewNotFoundException("Review not found"));

        return toResponse(review);
    }

    public List<AlbumReviewResponse> findBySpotifyAlbumId(String spotifyAlbumId) {
        return albumReviewRepository.findByAlbum_SpotifyAlbumId(spotifyAlbumId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AlbumReviewResponse> getMyReviews(String albumId, String artistId) {
        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        if (albumId != null && !albumId.isBlank()
                && artistId != null && !artistId.isBlank()) {
            return albumReviewRepository.findReviewsByUser_IdAndAlbum_Artist_SpotifyArtistIdAndAlbum_SpotifyAlbumIdOrderByListenedAtDesc(user.getId(), artistId, albumId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } else if (albumId != null && !albumId.isBlank()) {
            return albumReviewRepository.findReviewsByUser_IdAndAlbum_SpotifyAlbumIdOrderByListenedAtDesc(user.getId(), albumId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } else if (artistId != null && !artistId.isBlank()) {
            return albumReviewRepository.findReviewsByUser_IdAndAlbum_Artist_SpotifyArtistIdOrderByListenedAtDesc(user.getId(), artistId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } else {
            return albumReviewRepository.findReviewsByUser_IdOrderByListenedAtDesc(user.getId())
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }


    }

    public void deleteAlbumReview(Long reviewId) {
        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        AlbumReview review = albumReviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new AlbumReviewNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        albumReviewRepository.delete(review);
    }

    private void validateReview(Integer rating, String reviewText) {
        if (reviewText != null && !reviewText.isBlank() && rating == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes publicar una reseña sin una puntuación"
            );
        }
        if (rating != null && (rating < 0 || rating > 10)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La puntuación debe estar entre 0 y 10"
            );
        }
    }

    private AlbumReviewResponse toResponse(AlbumReview review) {
        Album album = review.getAlbum();

        return new AlbumReviewResponse(
                review.getId(),
                review.getUser().getUsername(),
                album.getSpotifyAlbumId(),
                review.isListened(),
                review.getRating(),
                review.getReviewText(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                review.getListenedAt()
        );
    }

    private Album findOrCreateAlbum(String spotifyAlbumId) {
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