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

        if (albumReviewRepository.existsByUser_IdAndAlbum_SpotifyAlbumId(
                user.getId(), albumId)) {
            throw new DuplicateAlbumReviewException(
                    "Ya tienes una entrada para este álbum");
        }

        Album album = findOrCreateAlbum(albumId);

        AlbumReview entry = new AlbumReview();
        entry.setUser(user);
        entry.setAlbum(album);
        entry.setListened(request.isListened());
        entry.setRating(request.getRating());
        entry.setReviewText(request.getReviewText());

        AlbumReview savedEntry = albumReviewRepository.save(entry);

        return toResponse(savedEntry);
    }

    public AlbumReviewResponse updateAlbumReview(
            Long reviewId,
            UpdateAlbumReviewRequest request
    ) {
        validateReview(request.getRating(), request.getReviewText());

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

        review.setListened(request.isListened());
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());

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

    public List<AlbumReviewResponse> getMyReviews() {
        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return albumReviewRepository.findReviewsByUser_Id(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
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
                review.getUpdatedAt()
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