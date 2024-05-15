package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.CreateEntityException;
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
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;
    private final ReviewLikeStorage reviewLikeStorage;
    private final UserFeedService userFeedService;

    public Review add(Review review) {
        filmService.getById(review.getFilmId());
        userService.getUserById(review.getUserId());
        Review added = reviewStorage.add(review);
        if (added.getId() == null) {
            throw new CreateEntityException(String.format("Отзыв не добавлен! %s", review));
        }
        log.info("Отзыв по id = {} успешно добавлен", review.getId());
        userFeedService.addInHistoryFeed(review.getUserId(), "REVIEW", "ADD", review.getId());
        log.info("Добавлено в историю создание отзыва пользователем id = {} фильму id = {}", review.getId(), review.getFilmId());
        return added;
    }

    public Review update(Review review) {
        getById(review.getId());
        Review updatedReview = reviewStorage.update(review);
        userFeedService.addInHistoryFeed(updatedReview.getUserId(), "REVIEW", "UPDATE", updatedReview.getId());
        log.info("Добавлено в историю обновление отзыва пользователем id = {} фильму id = {}", updatedReview.getId(), updatedReview.getFilmId());
        return updatedReview;
    }

    public Review delete(int id) {
        Review deleted = getById(id);
        deleteReviewLikes(id);
        reviewStorage.delete(id);
        userFeedService.addInHistoryFeed(deleted.getUserId(), "REVIEW", "REMOVE", deleted.getId());
        log.info("Добавлено в историю удаление отзыва пользователем id = {} фильму id = {}", deleted.getId(), deleted.getFilmId());
        return deleted;
    }

    /**
     * Delete all review likes and dislikes
     */
    public void deleteReviewLikes(int reviewId) {
        if (reviewLikeStorage.deleteLikes(reviewId)) {
            log.info("Удалены лайки отзыва");
        } else {
            throw new NotFoundException("Лайки отзыва не удалены!");
        }
    }

    public boolean deleteReviewLike(int reviewId, int userId) {
        if (reviewLikeStorage.deleteLike(reviewId, userId)) {
            log.info("Лайк к отзыву успешно удален");
            return true;
        } else {
            throw new NotFoundException("Лайк отзыва не удален!");
        }
    }

    public boolean deleteReviewDislike(int reviewId, int userId) {
        if (reviewLikeStorage.deleteDislike(reviewId, userId)) {
            log.info("Дизлайк к отзыву успешно удален");
            return true;
        } else {
            throw new NotFoundException("Дизлайк отзыва не удален!");
        }
    }

    public Collection<Review> getAll(Integer filmId, Integer count) {
        if (filmId == 0) {
            log.info("Получить все отзывы");
            log.info("Текущее количество всех отзывов: {}", getCount());
            return reviewStorage.getAll();
        } else {
            log.info("Получить все отзывы по фильму - {}, количество {}", filmId, count);
            Collection<Review> reviews = getByFilmId(filmId, count);
            log.info("Текущее количество всех отзывов на фильм: {}", reviews.size());
            return reviews;
        }
    }

    /**
     * Получить все отзывы к фильму по id фильма,
     * если указан count, получить только выбранное количество отзывов по фильму
     * в порядке от самых полезных к самым бесполезным
     */
    public Collection<Review> getByFilmId(int filmId, int count) {
        filmService.getById(filmId);
        return reviewStorage.getByFilmId(filmId).stream()
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Получить общее количество отзывов к фильмам
     */
    public int getCount() {
        return reviewStorage.getAll().size();
    }

    /**
     * Получить отзыв к фильму по id
     */
    public Review getById(int id) {
        Review review = reviewStorage.getById(id).orElseThrow(() ->
                new NotFoundException(String.format("Не найден отзыв по id = %s", id)));
        log.info("Получен отзыв по id = {}", id);
        return review;
    }

    /**
     * Поставить like отзыву
     */
    public Integer addLike(int id, int userId) {
        userService.getUserById(userId);
        Integer reviewLikeId = reviewLikeStorage.addLike(id, userId);
        increaseUseful(id);
        return reviewLikeId;
    }

    /**
     * Поставить dislike отзыву
     */
    public Integer addDislike(int id, int userId) {
        userService.getUserById(userId);
        Integer reviewDislikeId = reviewLikeStorage.addDislike(id, userId);
        decreaseUseful(id);
        return reviewDislikeId;
    }

    /**
     * Уменьшить полезность отзыва
     */
    public void increaseUseful(int reviewId) {
        getById(reviewId);
        reviewStorage.increaseUseful(reviewId);
    }

    /**
     * Увеличить полезность отзыва
     */
    public void decreaseUseful(int reviewId) {
        getById(reviewId);
        reviewStorage.decreaseUseful(reviewId);
    }

}
