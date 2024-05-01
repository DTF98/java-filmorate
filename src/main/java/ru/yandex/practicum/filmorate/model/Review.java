package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Отзыв к фильму
 */
@Data
@Builder
@Jacksonized
@AllArgsConstructor
@RequiredArgsConstructor
public class Review {

    @JsonProperty("reviewId")
    private Long id;

    @NotNull
    private final Long filmId;

    @NotNull
    private final Long userId;

    @NotNull
    @NotBlank
    @Size(max = 20000, message = "Review must be less than 20000 symbols")
    private String content;

    @Builder.Default
    private Integer useful = 0;

    @JsonProperty("isPositive")
    @NotNull
    private Boolean isPositive;

}
