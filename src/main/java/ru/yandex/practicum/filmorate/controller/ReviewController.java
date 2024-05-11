package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping()
    public ResponseEntity<Review> add(@Valid @RequestBody Review review) {
        log.info("Добавление отзыва к фильму");
        return ResponseEntity.ok(reviewService.add(review));
    }

    @PutMapping()
    public ResponseEntity<Review> update(@Valid @RequestBody Review review) {
        log.info("Обновление отзыва к фильму");
        return ResponseEntity.ok(reviewService.update(review));
    }

    /**
     * Получение всех отзывов по идентификатору фильма, если фильм не указан то все. Если кол-во не указано то 10.
     */
    @GetMapping()
    public ResponseEntity<Collection<Review>> getAll(@RequestParam(defaultValue = "0") Integer filmId,
                                                     @RequestParam(defaultValue = "10", required = false) Integer count) {
        log.info("Получение всех отзывов по идентификатору фильма");
        return ResponseEntity.ok(reviewService.getAll(filmId, count));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Integer> addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Пользователь {} ставит like отзыву {}", userId, id);
        return ResponseEntity.ok(reviewService.addLike(id, userId));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Integer> addDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("Пользователь {} ставит dislike отзыву {}", userId, id);
        return ResponseEntity.ok(reviewService.addDislike(id, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable int id) {
        log.info("Получить отзыв по ID фильма - {}", id);
        return ResponseEntity.ok(reviewService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> delete(@PathVariable int id) {
        log.info("Удаление отзыва к фильму {}", id);
        return ResponseEntity.ok(reviewService.delete(id));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Boolean> deleteLike(@PathVariable int id,
                                              @PathVariable int userId) {
        log.info("Удаление лайка к отзыву {}", id);
        return ResponseEntity.ok(reviewService.deleteReviewLike(id, userId));
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Boolean> deleteDislike(@PathVariable int id,
                                                 @PathVariable int userId) {
        log.info("Удаление дизлайка к отзыву {}", id);
        return ResponseEntity.ok(reviewService.deleteReviewDislike(id, userId));
    }

}
