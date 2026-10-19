package com.ivan.musiclog.controller;

import com.ivan.musiclog.dto.AlbumReviewResponse;
import com.ivan.musiclog.dto.CreateAlbumReviewRequest;
import com.ivan.musiclog.dto.UpdateAlbumReviewRequest;
import com.ivan.musiclog.entity.AlbumReview;
import com.ivan.musiclog.service.AlbumReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class AlbumReviewController {
    private final AlbumReviewService albumReviewService;

    public AlbumReviewController(AlbumReviewService albumReviewService) {
        this.albumReviewService = albumReviewService;
    }

    @PostMapping
    public AlbumReviewResponse createAlbumReview(
            @Valid @RequestBody CreateAlbumReviewRequest request) {

        return albumReviewService.createAlbumReview(request);
    }

    @PutMapping("/{reviewId}")
    public AlbumReviewResponse updateAlbumReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateAlbumReviewRequest request) {

        return albumReviewService.updateAlbumReview(reviewId, request);
    }

    @GetMapping("/{reviewId}")
    public AlbumReviewResponse getReviewById(@PathVariable Long reviewId){
        return albumReviewService.getReviewById(reviewId);
    }

    @GetMapping("/album/{spotifyAlbumId}")
    public List<AlbumReviewResponse> findBySpotifyAlbumId(@PathVariable String spotifyAlbumId){
        return albumReviewService.findBySpotifyAlbumId(spotifyAlbumId);
    }

    @GetMapping("/me")
    public List<AlbumReviewResponse> getMyReviews(){
        return albumReviewService.getMyReviews();
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReviewById(@PathVariable Long reviewId) {
        albumReviewService.deleteAlbumReview(reviewId);
    }

}
