package ru.yandex.practicum.filmorate.dao;

public interface ReviewLikeStorage {

    Integer addLike(Integer reviewId, Integer userId);

    boolean deleteLikes(Integer reviewId);

    Integer addDislike(Integer reviewId, Integer userId);

    boolean deleteLike(Integer reviewId, int userId);

    boolean deleteDislike(Integer reviewId, int userId);
}
