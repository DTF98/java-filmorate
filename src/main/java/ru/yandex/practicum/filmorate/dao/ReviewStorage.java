package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {

    Review add(Review review);

    Review update(Review review);

    Collection<Review> getAll();

    Optional<Review> getById(long id);

}
