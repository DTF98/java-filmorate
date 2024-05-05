package ru.yandex.practicum.filmorate.dao;

public interface ReviewLikeStorage {

    Integer addLike(Integer reviewId, Integer userId);

    void deleteLikes(Integer reviewId);

    Integer addDislike(Integer reviewId, Integer userId);

    void deleteLike(Integer reviewId, int userId);

    void deleteDislike(Integer reviewId, int userId);

}
