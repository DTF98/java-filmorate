package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;

    public ReviewService(ReviewStorage reviewStorage, FilmService filmService, UserService userService) {
        this.reviewStorage = reviewStorage;
        this.filmService = filmService;
        this.userService = userService;
    }

    public Review add(Review review) {
        filmService.getById(review.getFilmId().intValue()); // если фильма нет, то метод пробросит ошибку
        userService.getById(review.getUserId().intValue()); // если пользователя нет, то метод пробросит ошибку

        return reviewStorage.add(review);
    }

    public Review update(Review review) {
        getById(review.getId()); // если отзыва нет, то метод пробросит ошибку

        return reviewStorage.update(review);
    }

    public Collection<Review> getAll() {
        return reviewStorage.getAll();
    }

    public long getCount() {
        return reviewStorage.getAll().size();
    }

    public Review getById(long id) {
        return reviewStorage.getById(id).orElseThrow(() -> {
            String errorText = "Отзыв с таким Id не найден: " + id;
            log.error(errorText);
            return new NotFoundException(errorText);
        });
    }

}
