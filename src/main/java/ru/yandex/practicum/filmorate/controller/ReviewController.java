package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccess;
import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccessList;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping()
    public ResponseEntity<Review> add(@Valid @RequestBody Review review) {
        log.info("Добавление отзыва к фильму");

        Review addedFilm = reviewService.add(review);
        log.info("Отзыв успешно добавлен");

        return respondSuccess(addedFilm);
    }

    @PutMapping()
    public ResponseEntity<Review> update(@Valid @RequestBody Review review) {
        log.info("Обновление отзыва к фильму");

        Review updatedReview = reviewService.update(review);
        log.info("Отзыв успешно обновлен");

        return respondSuccess(updatedReview);
    }

    /**
     * Получение всех отзывов по идентификатору фильма, если фильм не указан то все. Если кол-во не указано то 10.
     */
    @GetMapping()
    public ResponseEntity<Collection<Review>> getAll(@RequestParam(defaultValue = "0") Integer filmId,
                                                     @RequestParam(defaultValue = "10", required = false) Integer count) {

        // Если фильм не передается в параметрах, возвращаем все отзывы
        if (filmId == 0) {
            log.info("Получить все отзывы ");
            log.info("Текущее количество всех отзывов: {}", reviewService.getCount());

            return respondSuccessList(reviewService.getAll());
        } else {
            log.info("Получить все отзывы по фильму - {}, количество {}", filmId, count);
            Collection<Review> reviews = reviewService.getByFilmId(filmId, count);
            log.info("Текущее количество всех отзывов на фильм: {}", reviews.size());

            return respondSuccess(reviews);
        }

    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Пользователь {} ставит like отзыву {}", userId, id);

        reviewService.addLike(id, userId);
        log.info("Like отзыву успешно поставлен");

        return respondSuccess();
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Film> addDislike(@PathVariable long id, @PathVariable long userId) {
        log.info("Пользователь {} ставит dislike отзыву {}", userId, id);

        reviewService.addDislike(id, userId);
        log.info("Dislike отзыву успешно поставлен");

        return respondSuccess();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable long id) {
        log.info("Получить отзыв по ID фильма - {}", id);

        return respondSuccess(reviewService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Long> delete(@PathVariable long id) {
        log.info("Удаление отзыва к фильму {}", id);

        long reviewId = reviewService.delete(id);
        log.info("Отзыв успешно удален");

        return respondSuccess(reviewId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Long> deleteLike(@PathVariable long id,
                                           @PathVariable long userId) {
        log.info("Удаление лайка к отзыву {}", id);

        reviewService.deleteReviewLike(id, userId);
        log.info("Лайк к отзыву успешно удален");

        return respondSuccess();
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Long> deleteDislike(@PathVariable long id,
                                              @PathVariable long userId) {
        log.info("Удаление дизлайка к отзыву {}", id);

        reviewService.deleteReviewDislike(id, userId);
        log.info("Дизлайк к отзыву успешно удален");

        return respondSuccess();
    }

}