package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {

    Review add(Review review);

    Review update(Review review);

    void delete(int id);

    void increaseUseful(int reviewId);

    void decreaseUseful(int reviewId);

    Collection<Review> getAll();

    Optional<Review> getById(int id);

    Collection<Review> getByFilmId(int filmId);
}
