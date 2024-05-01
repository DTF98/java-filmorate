package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.dao.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Сервис отзывов к фильмам
 */
@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;
    private final ReviewLikeStorage reviewLikeStorage;

    public ReviewService(ReviewStorage reviewStorage, FilmService filmService, UserService userService, ReviewLikeStorage reviewLikeStorage) {
        this.reviewStorage = reviewStorage;
        this.filmService = filmService;
        this.userService = userService;
        this.reviewLikeStorage = reviewLikeStorage;
    }

    public Review add(Review review) {
        filmService.getById(review.getFilmId().intValue()); // если фильма нет, то метод пробросит ошибку
        userService.getById(review.getUserId().intValue()); // если пользователя нет, то метод пробросит ошибку

        return reviewStorage.add(review);
    }

    public Review update(Review review) {
        getById(review.getId()); // если отзыва нет, то метод пробросит ошибку

        Review updatedReview = reviewStorage.update(review);

        return getById(updatedReview.getId());
    }

    public long delete(long id) {
        deleteReviewLikes(id); // delete all review likes and dislikes

        reviewStorage.delete(id);

        return id;
    }

    /**
     * Delete all review likes and dislikes
     */
    public void deleteReviewLikes(long reviewId) {
        reviewLikeStorage.deleteLikes(reviewId);
    }

    public void deleteReviewLike(long reviewId, long userId) {
        reviewLikeStorage.deleteLike(reviewId, userId);
    }

    public void deleteReviewDislike(long reviewId, long userId) {
        reviewLikeStorage.deleteDislike(reviewId, userId);
    }

    public Collection<Review> getAll() {
        return reviewStorage.getAll();
    }

    /**
     * Получить все отзывы к фильму по id фильма,
     * если указан count, получить только выбранное количество отзывово по фильму
     * в порядке от самых полезных к самым бесполезным
     */
    public Collection<Review> getByFilmId(long filmId, int count) {
        filmService.getById((int) filmId);

        return reviewStorage.getByFilmId(filmId).stream()
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Получить общее количество отзывов к фильмам
     */
    public long getCount() {
        return reviewStorage.getAll().size();
    }

    /**
     * Получить отзыв к фильму по id
     */
    public Review getById(long id) {
        return reviewStorage.getById(id).orElseThrow(() -> {
            String errorText = "Отзыв с таким Id не найден: " + id;
            log.error(errorText);
            return new NotFoundException(errorText);
        });
    }

    /**
     * Поставить like отзыву
     */
    public void addLike(long id, long userId) {
        userService.getById((int) userId); // если пользователя нету то метод сам пробросит ошибку

        reviewLikeStorage.addLike(id, userId); // добавляем лайк отзыву

        increaseUseful(id); // увеличить полезность отзыва
    }

    /**
     * Поставить dislike отзыву
     */
    public void addDislike(long id, long userId) {
        userService.getById((int) userId); // если пользователя нету то метод сам пробросит ошибку

        reviewLikeStorage.addDislike(id, userId); // добавляем дизлайк отзыву

        decreaseUseful(id); // уменьшить полезность отзыва
    }

    /**
     * Уменьшить полезность отзыва
     */
    public void increaseUseful(long reviewId) {
        Review review = getById(reviewId); // если отзыва нет, то метод пробросит ошибку

        reviewStorage.increaseUseful(reviewId);

        Review receivedReview = getById(reviewId);

        log.info(review.getUseful() + " - " + receivedReview.getUseful());
        System.out.println();
    }

    /**
     * Увеличить полезность отзыва
     */
    public void decreaseUseful(long reviewId) {
        Review review = getById(reviewId); // если отзыва нет, то метод пробросит ошибку

        reviewStorage.decreaseUseful(reviewId);

        Review receivedReview = getById(reviewId);

        log.info(review.getUseful() + " - " + receivedReview.getUseful());
        System.out.println();
    }

}
