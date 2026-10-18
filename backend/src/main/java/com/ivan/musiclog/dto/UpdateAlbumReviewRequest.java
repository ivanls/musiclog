package com.ivan.musiclog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateAlbumReviewRequest {

    @NotNull
    @Min(1)
    @Max(10)
    private Integer rating;
    private String reviewText;
}
