package com.ivan.musiclog.service;

import com.ivan.musiclog.dto.CreateAlbumReviewRequest;
import com.ivan.musiclog.entity.AlbumReview;
import com.ivan.musiclog.entity.User;
import com.ivan.musiclog.repository.AlbumReviewRepository;
import com.ivan.musiclog.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AlbumReviewService {

    private final AlbumReviewRepository albumReviewRepository;
    private final UserRepository userRepository;

    public AlbumReviewService(AlbumReviewRepository albumReviewRepository, UserRepository userRepository) {
        this.albumReviewRepository = albumReviewRepository;
        this.userRepository = userRepository;
    }

    public AlbumReview createAlbumReview(CreateAlbumReviewRequest review){
        AlbumReview request = new AlbumReview();

        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String albumId = review.getSpotifyAlbumId();
        String albumReview = review.getReviewText();
        Integer albumRating = review.getRating();

        request.setUser(user);
        request.setSpotifyAlbumId(albumId);
        request.setReviewText(albumReview);
        request.setRating(albumRating);

        return albumReviewRepository.save(request);
    }
}
