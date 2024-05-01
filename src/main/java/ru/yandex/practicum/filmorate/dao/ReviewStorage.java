package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {

    Review add(Review review);

    Review update(Review review);

    long delete(long id);

    void increaseUseful(long reviewId);

    void decreaseUseful(long reviewId);

    Collection<Review> getAll();

    Optional<Review> getById(long id);

    Collection<Review> getByFilmId(long filmId);

}
