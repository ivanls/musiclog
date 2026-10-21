
package com.ivan.musiclog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateAlbumReviewRequest {

    @NotBlank
    private String spotifyAlbumId;

    private boolean listened = false;

    @Min(1)
    @Max(10)
    private Integer rating;

    private String reviewText;

    private LocalDateTime listenedAt;
}
