package ru.yandex.practicum.filmorate.dao;

public interface ReviewLikeStorage {

    Long addLike(Long reviewId, Long userId);

    void deleteLikes(Long reviewId);

    Long addDislike(Long reviewId, Long userId);

    void deleteLike(Long reviewId, long userId);

    void deleteDislike(Long reviewId, long userId);

}
