
package com.ivan.musiclog.service;

import com.ivan.musiclog.dto.AlbumReviewResponse;
import com.ivan.musiclog.dto.CreateAlbumReviewRequest;
import com.ivan.musiclog.dto.UpdateAlbumReviewRequest;
import com.ivan.musiclog.entity.AlbumReview;
import com.ivan.musiclog.entity.User;
import com.ivan.musiclog.exception.AccessDeniedException;
import com.ivan.musiclog.exception.AlbumReviewNotFoundException;
import com.ivan.musiclog.exception.DuplicateAlbumReviewException;
import com.ivan.musiclog.repository.AlbumReviewRepository;
import com.ivan.musiclog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AlbumReviewService {

    private final AlbumReviewRepository albumReviewRepository;
    private final UserRepository userRepository;

    public AlbumReviewService(
            AlbumReviewRepository albumReviewRepository,
            UserRepository userRepository
    ) {
        this.albumReviewRepository = albumReviewRepository;
        this.userRepository = userRepository;
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

        if (albumReviewRepository.existsByUser_IdAndSpotifyAlbumId(
                user.getId(), albumId)) {
            throw new DuplicateAlbumReviewException(
                    "Ya tienes una entrada para este álbum");
        }

        AlbumReview entry = new AlbumReview();

        entry.setUser(user);
        entry.setSpotifyAlbumId(albumId);
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
            throw new AccessDeniedException("No puedes editar esta entrada");
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
        return albumReviewRepository.findBySpotifyAlbumId(spotifyAlbumId)
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

        albumReviewRepository.deleteById(reviewId);
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
        return new AlbumReviewResponse(
                review.getId(),
                review.getUser().getUsername(),
                review.getSpotifyAlbumId(),
                review.isListened(),
                review.getRating(),
                review.getReviewText(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
