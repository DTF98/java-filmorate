package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Оценка отзыва
 */
@Data
@Jacksonized
@Builder
@RequiredArgsConstructor
@AllArgsConstructor

public class ReviewLike {
    private Long id;
    private final Long reviewId;
    private final Long userId;
    private final Boolean type;
}
