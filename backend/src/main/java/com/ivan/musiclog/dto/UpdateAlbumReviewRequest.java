
package com.ivan.musiclog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateAlbumReviewRequest {

    private Boolean listened;

    @Min(1)
    @Max(10)
    private Integer rating;

    private String reviewText;

    private LocalDateTime listenedAt;
}
