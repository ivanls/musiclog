package com.ivan.musiclog.controller;

import com.ivan.musiclog.dto.CreateAlbumReviewRequest;
import com.ivan.musiclog.entity.AlbumReview;
import com.ivan.musiclog.service.AlbumReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class AlbumReviewController {
    private final AlbumReviewService albumReviewService;

    public AlbumReviewController(AlbumReviewService albumReviewService) {
        this.albumReviewService = albumReviewService;
    }

    @PostMapping
    public AlbumReview createAlbumReview(
            @Valid @RequestBody CreateAlbumReviewRequest request) {

        return albumReviewService.createAlbumReview(request);
    }

    @PutMapping
    public AlbumReview updateAlbumReview(
            @Valid @RequestBody UpdateAlbumReview request) {
        return albumReviewService.updateAlbumReview(request);
    }

}
