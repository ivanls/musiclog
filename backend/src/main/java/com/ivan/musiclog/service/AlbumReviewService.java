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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlbumReviewService {

    private final AlbumReviewRepository albumReviewRepository;
    private final UserRepository userRepository;

    public AlbumReviewService(AlbumReviewRepository albumReviewRepository, UserRepository userRepository) {
        this.albumReviewRepository = albumReviewRepository;
        this.userRepository = userRepository;
    }

    public AlbumReview createAlbumReview(CreateAlbumReviewRequest review){

        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String albumId = review.getSpotifyAlbumId();
        String albumReview = review.getReviewText();
        Integer albumRating = review.getRating();

        if (albumReviewRepository.existsByUser_IdAndSpotifyAlbumId(user.getId(), albumId)) {
            throw new DuplicateAlbumReviewException("Ya has publicado una reseña para este álbum");
        }

        AlbumReview request = new AlbumReview();

        request.setUser(user);
        request.setSpotifyAlbumId(albumId);
        request.setReviewText(albumReview);
        request.setRating(albumRating);

        return albumReviewRepository.save(request);
    }

    public AlbumReview updateAlbumReview(Long reviewId, UpdateAlbumReviewRequest request) {

        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        AlbumReview reviewToChange = albumReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AlbumReviewNotFoundException("Review not found"));

        if (!reviewToChange.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("No puedes editar esta reseña");
        }

        reviewToChange.setReviewText(request.getReviewText());
        reviewToChange.setRating(request.getRating());

        return albumReviewRepository.save(reviewToChange);
    }

    public AlbumReviewResponse getReviewById(Long reviewId){
        AlbumReview review = albumReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AlbumReviewNotFoundException("Review not found"));

        return toResponse(review);
    }

    public List<AlbumReviewResponse> findBySpotifyAlbumId(String spotifyAlbumId) {
        List<AlbumReview> albumReviewList =
                albumReviewRepository.findBySpotifyAlbumId(spotifyAlbumId);

        return albumReviewList.stream()
                .map(review -> toResponse(review))
                .toList();
    }

    public List<AlbumReviewResponse> getMyReviews(){

        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<AlbumReview> albumReviewList =
                albumReviewRepository.findReviewsByUser_Id(user.getId());

        return albumReviewList.stream()
                .map(review -> toResponse(review))
                .toList();
    }

    public void deleteAlbumReview(Long reviewId){

        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        AlbumReview review = albumReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AlbumReviewNotFoundException("Review not found"));

        if(!review.getUser().getId().equals(user.getId())){
            throw new AccessDeniedException("Access denied");
        }

        albumReviewRepository.deleteById(reviewId);
    }

    private AlbumReviewResponse toResponse(AlbumReview review) {
        return new AlbumReviewResponse(
                review.getId(),
                review.getUser().getUsername(),
                review.getSpotifyAlbumId(),
                review.getRating(),
                review.getReviewText(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

}
