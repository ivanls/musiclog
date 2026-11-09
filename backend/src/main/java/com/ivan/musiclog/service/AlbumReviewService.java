package com.ivan.musiclog.service;

import com.ivan.musiclog.dto.*;
import com.ivan.musiclog.entity.*;
import com.ivan.musiclog.exception.AccessDeniedException;
import com.ivan.musiclog.exception.AlbumReviewNotFoundException;
import com.ivan.musiclog.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AlbumReviewService {
    private final AlbumReviewRepository albumReviewRepository;
    private final UserRepository userRepository;
    private final AlbumListenRepository albumListenRepository;
    private final AlbumService albumService;

    public AlbumReviewService(AlbumReviewRepository albumReviewRepository, UserRepository userRepository, AlbumListenRepository albumListenRepository, AlbumService albumService) {
        this.albumReviewRepository = albumReviewRepository;
        this.userRepository = userRepository;
        this.albumListenRepository = albumListenRepository;
        this.albumService = albumService;
    }

    @Transactional
    public AlbumReviewResponse createAlbumReview(CreateAlbumReviewRequest request) {
        validateReview(request.getRating(), request.getReviewText());

        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        String albumId = request.getSpotifyAlbumId();
        Album album = albumService.findOrCreateAlbum(albumId);

        AlbumListen listen = albumListenRepository
                .findByUser_IdAndAlbum_SpotifyAlbumId(user.getId(), albumId)
                .orElse(null);

        if (listen == null) {
            AlbumListen newListen = new AlbumListen();
            newListen.setUser(user);
            newListen.setAlbum(album);
            newListen.setListened(true);
            newListen.setListenedAt(LocalDateTime.now());

            listen = albumListenRepository.save(newListen);
        } else {
            if (!listen.isListened()) {
                listen.setListened(true);
                listen = albumListenRepository.save(listen);
            }
        }

        AlbumReview review = new AlbumReview();
        review.setAlbumListen(listen);
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());

        AlbumReview savedReview = albumReviewRepository.save(review);
        return toResponse(savedReview);
    }

    public AlbumReviewResponse updateAlbumReview(
            Long reviewId,
            UpdateAlbumReviewRequest request
    ) {
        //Search user
        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
        //Search review
        AlbumReview review = albumReviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new AlbumReviewNotFoundException("Review not found"));
        //The user is the owner?
        if (!review.getAlbumListen().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "No puedes editar esta entrada");
        }
        //Calculate final data
        Integer finalRating = (request.getRating() == null
                ? review.getRating() : request.getRating());
        String finalReviewText = (request.getReviewText() == null
                ? review.getReviewText() : request.getReviewText());
        //Validate data
        validateReview(finalRating, finalReviewText);
        //Update rating and reviewText
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getReviewText() != null) {
            review.setReviewText(request.getReviewText());
        }
        //Save review
        AlbumReview updatedReview = albumReviewRepository.save(review);
        //Return response
        return toResponse(updatedReview);
    }

    public AlbumReviewResponse getReviewById(Long reviewId) {
        AlbumReview review = albumReviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new AlbumReviewNotFoundException("Review not found"));

        return toResponse(review);
    }

    public List<AlbumReviewResponse> findBySpotifyAlbumId(String spotifyAlbumId) {
        return albumReviewRepository.findByAlbumListen_Album_SpotifyAlbumId(spotifyAlbumId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<AlbumReviewResponse> getMyReviews(String albumId, String artistId, Pageable pageable) {
        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        if (albumId != null && !albumId.isBlank()
                && artistId != null && !artistId.isBlank()) {

            return albumReviewRepository
                    .findByAlbumListen_User_IdAndAlbumListen_Album_Artist_SpotifyArtistIdAndAlbumListen_Album_SpotifyAlbumIdOrderByAlbumListen_ListenedAtDesc(
                            user.getId(), artistId, albumId, pageable)
                    .map(this::toResponse);

        } else if (albumId != null && !albumId.isBlank()) {

            return albumReviewRepository
                    .findByAlbumListen_User_IdAndAlbumListen_Album_SpotifyAlbumIdOrderByAlbumListen_ListenedAtDesc(
                            user.getId(), albumId, pageable)
                    .map(this::toResponse);

        } else if (artistId != null && !artistId.isBlank()) {

            return albumReviewRepository
                    .findByAlbumListen_User_IdAndAlbumListen_Album_Artist_SpotifyArtistIdOrderByAlbumListen_ListenedAtDesc(
                            user.getId(), artistId, pageable)
                    .map(this::toResponse);

        } else {

            return albumReviewRepository
                    .findByAlbumListen_User_IdOrderByAlbumListen_ListenedAtDesc(
                            user.getId(), pageable)
                    .map(this::toResponse);
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

        if (!review.getAlbumListen().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        albumReviewRepository.delete(review);
    }

    private void validateReview(Integer rating, String reviewText) {
        if (rating == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes publicar una reseña sin una puntuación"
            );
        }

        if (rating < 0 || rating > 10) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La puntuación debe estar entre 0 y 10"
            );
        }
    }

    private AlbumReviewResponse toResponse(AlbumReview review) {
        AlbumListen listen = review.getAlbumListen();

        Album album = listen.getAlbum();

        return new AlbumReviewResponse(
                review.getId(),
                listen.getUser().getUsername(),
                album.getSpotifyAlbumId(),
                listen.isListened(),
                review.getRating(),
                review.getReviewText(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                listen.getListenedAt()
        );
    }
}