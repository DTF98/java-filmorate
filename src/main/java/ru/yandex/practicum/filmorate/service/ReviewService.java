package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.dao.UserFeedStorage;
import ru.yandex.practicum.filmorate.exception.ApplicationException;
import ru.yandex.practicum.filmorate.exception.CreateEntityException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
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
    private final UserFeedStorage userFeedStorage;

    public Review add(Review review) {
        filmService.getById(review.getFilmId());
        userService.getUserById(review.getUserId());
        try {
            Review added = reviewStorage.add(review);
            if (added.getId() == 0) {
                throw new CreateEntityException(String.format("Отзыв не добавлен! %s", review));
            }
            log.info("Отзыв по id = {} успешно добавлен", review.getId());
            if (userFeedStorage.addInHistory(review.getUserId(), "REVIEW", "ADD", review.getId())) {
                log.info("Добавлено в историю создание отзыва пользователем id = {} фильму id = {}", review.getId(), review.getFilmId());
            }
            return added;
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении отзыва");
        }
        throw new ApplicationException("Ошибка при добавлении отзыва");
    }

    public Review update(Review review) {
        try {
            Review updatedReview = reviewStorage.update(review);
            if (updatedReview.getId() == 0) {
                throw new NotFoundException("Не найден отзыв!");
            }
            if (userFeedStorage.addInHistory(updatedReview.getUserId(), "REVIEW", "UPDATE", updatedReview.getId())) {
                log.info("Добавлено в историю обновление отзыва пользователем id = {} фильму id = {}", updatedReview.getId(), updatedReview.getFilmId());
            }
            return updatedReview;
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении отзыва", e);
        }
        throw new ApplicationException("Ошибка при обновлении отзыва");
    }

    public int delete(int id) {
        try {
            if (reviewStorage.isExistReviewById(id)) {
                Optional<Review> delete = reviewStorage.getById(id);
                deleteReviewLikes(id);
                reviewStorage.delete(id);
                if (delete.isPresent()) {
                    if (userFeedStorage.addInHistory(delete.get().getUserId(), "REVIEW", "REMOVE", delete.get().getId()))
                        log.info("Добавлено в историю удаление отзыва пользователем id = {} фильму id = {}", delete.get().getId(), delete.get().getFilmId());
                }
                return id;
            } else {
                throw new NotFoundException("Не найден отзыв!");
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении отзыва", e);
        }
        throw new ApplicationException("Ошибка при удалении отзыва");
    }

    /**
     * Delete all review likes and dislikes
     */
    public void deleteReviewLikes(int reviewId) {
        try {
            if (reviewLikeStorage.deleteLikes(reviewId)) {
                log.info("Удалены лайки отзыва");
            } else {
                throw new NotFoundException("Лайки отзыва не удалены!");
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении лайков отзыва", e);
        }
    }

    public boolean deleteReviewLike(int reviewId, int userId) {
        try {
            if (reviewLikeStorage.deleteLike(reviewId, userId)) {
                log.info("Лайк к отзыву успешно удален");
            } else {
                throw new NotFoundException("Лайк отзыва не удален!");
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении лайка отзыву", e);
        }
        throw new ApplicationException("Ошибка при удалении лайка отзыву");
    }

    public boolean deleteReviewDislike(int reviewId, int userId) {
        try {
            if (reviewLikeStorage.deleteDislike(reviewId, userId)) {
                log.info("Дизлайк к отзыву успешно удален");
            } else {
                throw new NotFoundException("Дизлайк отзыва не удален!");
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении дизлайка отзыву", e);
        }
        throw new ApplicationException("Ошибка при удалении дизлайка отзыву");
    }

    public Collection<Review> getAll(Integer filmId, Integer count) {
        try {
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
        } catch (DataAccessException e) {
            log.error("Ошибка при получении отзывов", e);
        }
        throw new ApplicationException("Ошибка при получении отзывов");
    }

    /**
     * Получить все отзывы к фильму по id фильма,
     * если указан count, получить только выбранное количество отзывов по фильму
     * в порядке от самых полезных к самым бесполезным
     */
    public Collection<Review> getByFilmId(int filmId, int count) {
        filmService.getById(filmId);
        try {
            return reviewStorage.getByFilmId(filmId).stream()
                    .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                    .limit(count)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("Ошибка при получении отзывов к фильму", e);
        }
        throw new ApplicationException("Ошибка при получении отзывов к фильму");
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
        try {
            Optional<Review> review = reviewStorage.getById(id);
            if (review.isPresent()) {
                return review.get();
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении отзыва", e);
            throw new NotFoundException("Отзыв с таким Id не найден");
        }
        throw new ApplicationException("Ошибка при получении отзыва");
    }

    /**
     * Поставить like отзыву
     */
    public Integer addLike(int id, int userId) {
        userService.getUserById(userId);
        try {
            Integer reviewLikeId = reviewLikeStorage.addLike(id, userId);
            increaseUseful(id);
            return reviewLikeId;
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении лайка отзыву", e);
        }
        throw new ApplicationException("Ошибка при добавлении лайка отзыву");
    }

    /**
     * Поставить dislike отзыву
     */
    public Integer addDislike(int id, int userId) {
        userService.getUserById(userId);
        try {
            Integer reviewDislikeId = reviewLikeStorage.addDislike(id, userId);
            decreaseUseful(id);
            return reviewDislikeId;
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении дизлайка отзыву", e);
        }
        throw new ApplicationException("Ошибка при добавлении дизлайка отзыву");

    }

    /**
     * Уменьшить полезность отзыва
     */
    public void increaseUseful(int reviewId) {
        getById(reviewId);
        try {
            reviewStorage.increaseUseful(reviewId);
        } catch (DataAccessException e) {
            log.error("Ошибка при уменьшении полезности отзыва", e);
        }
    }

    /**
     * Увеличить полезность отзыва
     */
    public void decreaseUseful(int reviewId) {
        getById(reviewId);
        try {
            reviewStorage.decreaseUseful(reviewId);
        } catch (DataAccessException e) {
            log.error("Ошибка при увеличении полезности отзыва", e);
        }
    }

}
